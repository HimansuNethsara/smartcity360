package lk.jiat.smartcity360.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lk.jiat.smartcity360.dto.ChangePasswordRequest;
import lk.jiat.smartcity360.dto.LoginRequest;
import lk.jiat.smartcity360.dto.ProfileRequest;
import lk.jiat.smartcity360.dto.RegisterRequest;
import lk.jiat.smartcity360.dto.SessionView;
import lk.jiat.smartcity360.security.JwtService;
import lk.jiat.smartcity360.security.LoginThrottle;
import lk.jiat.smartcity360.service.AuthService;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;

import java.util.Map;

@Path("/auth")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final String BAD_LOGIN = "Invalid username or password.";

    @Inject
    private AuthService authService;
    @Inject
    private JwtService jwtService;
    @Inject
    private LoginThrottle throttle;
    @Context
    private HttpServletRequest request;
    @Context
    private SecurityContext security;

    @POST
    @Path("/login")
    @Public
    public Response login(LoginRequest body) {
        String username = body == null || body.getUsername() == null ? "" : body.getUsername().trim();
        String password = body == null || body.getPassword() == null ? "" : body.getPassword();
        if (username.isEmpty() || password.isEmpty()) {
            return error(400, "bad_request", "Enter your username and password.");
        }
        long locked = throttle.secondsLocked(username);
        if (locked > 0) {
            return error(429, "locked", "Too many failed attempts. Try again in " + locked + " seconds.");
        }
        if (!realmAccepts(username, password)) {
            throttle.failed(username);
            return error(401, "bad_credentials", BAD_LOGIN);
        }
        SessionView user;
        try {
            user = authService.forLogin(username);
        } catch (BusinessRuleException e) {
            return error(401, "bad_credentials", e.getMessage());
        }
        throttle.succeeded(username);
        return withCookie(Response.ok(), user);
    }

    @POST
    @Path("/register")
    @Public
    public Response register(RegisterRequest body) {
        if (body == null) {
            return error(400, "bad_request", "Missing registration details.");
        }
        SessionView user = authService.register(body);
        return withCookie(Response.status(201), user);
    }

    @POST
    @Path("/logout")
    @Public
    public Response logout() {
        return Response.ok(Map.of("message", "Logged out")).cookie(cookie("", 0)).build();
    }

    @GET
    @Path("/me")
    @Roles({"ADMIN", "CITY_MANAGER", "SUPERVISOR", "STAFF", "CITIZEN"})
    public SessionView me() {
        return authService.me(current().id());
    }

    @PUT
    @Path("/profile")
    @Roles({"ADMIN", "CITY_MANAGER", "SUPERVISOR", "STAFF", "CITIZEN"})
    public SessionView profile(ProfileRequest body) {
        return authService.updateProfile(current().id(), body);
    }

    @POST
    @Path("/change-password")
    @Roles({"ADMIN", "CITY_MANAGER", "SUPERVISOR", "STAFF", "CITIZEN"})
    public Response changePassword(ChangePasswordRequest body) {
        long id = current().id();
        authService.changePassword(id, body);
        jwtService.revokeUser(id);                       // every older token stops working...
        return withCookie(Response.ok(), authService.me(id));   // and this browser gets a fresh one
    }

    // ---------------------------------------------------------------- helpers

    private AuthenticatedUser current() {
        return (AuthenticatedUser) security.getUserPrincipal();
    }

    private boolean realmAccepts(String username, String password) {
        try {
            if (request.getUserPrincipal() != null) {
                request.logout();
            }
            request.login(username, password);
            return true;
        } catch (ServletException e) {
            return false;
        } finally {
            try {
                request.logout();
            } catch (ServletException ignored) {
                // nothing was logged in
            }
            HttpSession s = request.getSession(false);
            if (s != null) {
                s.invalidate();
            }
        }
    }

    private Response withCookie(Response.ResponseBuilder builder, SessionView user) {
        String token = jwtService.issue(user.getId(), user.getUsername(), user.getRoles());
        return builder.entity(user.withToken(token)).cookie(cookie(token, (int) jwtService.ttlSeconds())).build();
    }

    private NewCookie cookie(String value, int maxAgeSeconds) {
        return new NewCookie.Builder(JwtAuthFilter.COOKIE)
                .value(value)
                .path(request.getContextPath().isEmpty() ? "/" : request.getContextPath())
                .maxAge(maxAgeSeconds)
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite(NewCookie.SameSite.STRICT)
                .build();
    }

    private static Response error(int status, String code, String message) {
        return Response.status(status).type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", code, "message", message)).build();
    }
}
