package lk.jiat.smartcity360.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lk.jiat.smartcity360.dto.ResetPasswordRequest;
import lk.jiat.smartcity360.dto.UserCreateRequest;
import lk.jiat.smartcity360.dto.UserUpdateRequest;
import lk.jiat.smartcity360.dto.UserView;
import lk.jiat.smartcity360.service.UserAdminService;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;

import java.util.List;

@Path("/users")
@Roles({"ADMIN"})
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserAdminResource {

    @Inject
    private UserAdminService service;
    @Context
    private SecurityContext security;

    @GET
    public List<UserView> list(@QueryParam("role") String role,
                               @QueryParam("enabled") String enabled,
                               @QueryParam("q") String q) {
        return service.listUsers(role, parseFlag(enabled), q);
    }

    @POST
    public Response create(UserCreateRequest request) {
        UserView created = service.create(request, Caller.of(security).id());
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public UserView update(@PathParam("id") Long id, UserUpdateRequest request) {
        return service.update(id, request, Caller.of(security).id());
    }

    @POST
    @Path("/{id}/reset-password")
    public Response resetPassword(@PathParam("id") Long id, ResetPasswordRequest request) {
        service.resetPassword(id, request, Caller.of(security).id());
        return Response.noContent().build();
    }

    private static Boolean parseFlag(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (value.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        throw new BusinessRuleException("enabled must be true or false.");
    }
}