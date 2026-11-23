package lk.jiat.smartcity360.web;

import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.jiat.smartcity360.security.JwtClaims;
import lk.jiat.smartcity360.security.JwtService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@WebFilter(urlPatterns = "/*", dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD})
public class PageGuardFilter implements Filter {

    private static final String COOKIE = "sc360_token";
    private static final List<String> ROLE_ORDER = List.of("ADMIN", "CITY_MANAGER", "SUPERVISOR", "STAFF", "CITIZEN");
    private static final Map<String, String> ROLE_LABEL = Map.of(
            "ADMIN", "Admin", "CITY_MANAGER", "City manager", "SUPERVISOR", "Supervisor",
            "STAFF", "Staff", "CITIZEN", "Citizen");
    private static final Set<String> STAFF = Set.of("ADMIN", "CITY_MANAGER", "SUPERVISOR", "STAFF");

    private static final Map<String, Set<String>> PAGE_ROLES = Map.of(
            "/ui/meters.jsp", STAFF,
            "/ui/zones.jsp", STAFF,
            "/ui/streetlights.jsp", STAFF,
            "/ui/policies.jsp", STAFF,
            "/ui/users.jsp", Set.of("ADMIN"),
            "/ui/reports.jsp", STAFF
    );

    @Inject
    private JwtService jwtService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String ctx = req.getContextPath();
        String path = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
        String key = path.toLowerCase(Locale.ROOT);          // Windows file systems ignore case

        // Clean URLs: CleanUrlFilter forwards /ui/meters to /ui/meters.jsp. The rules below run on that
        // forwarded pass, where the path is the real file name, so the first pass is just handed on.
        if (request.getDispatcherType() == DispatcherType.REQUEST && CleanUrlFilter.isCleanPage(key)) {
            chain.doFilter(request, response);
            return;
        }

        JwtClaims claims = claims(req);
        if (claims != null) {
            expose(req, claims);
        }

        if (isOpen(key)) {
            if (claims != null && (key.equals("/login.jsp") || key.equals("/register.jsp"))) {
                resp.sendRedirect(ctx + "/");        // already logged in
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        if (claims == null) {
            String back = req.getRequestURI() + (req.getQueryString() == null ? "" : "?" + req.getQueryString());
            resp.sendRedirect(ctx + "/login?next=" + URLEncoder.encode(back, StandardCharsets.UTF_8));
            return;
        }
        Set<String> needed = PAGE_ROLES.get(key);
        if (needed != null && claims.roles().stream().noneMatch(needed::contains)) {
            resp.sendRedirect(ctx + "/ui/forbidden");
            return;
        }
        resp.setHeader("Cache-Control", "no-store");          // no cached pages after logging out
        chain.doFilter(request, response);
    }

    private static boolean isOpen(String key) {
        return key.startsWith("/api/") || key.startsWith("/css/") || key.startsWith("/js/")
                || key.startsWith("/errors/")
                || key.equals("/login.jsp") || key.equals("/register.jsp") || key.equals("/favicon.ico");
    }

    private JwtClaims claims(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (COOKIE.equals(c.getName())) {
                return jwtService.verify(c.getValue()).orElse(null);
            }
        }
        return null;
    }

    private static void expose(HttpServletRequest req, JwtClaims claims) {
        Set<String> roles = new HashSet<>(claims.roles());
        String top = ROLE_ORDER.stream().filter(roles::contains).findFirst().orElse("");
        req.setAttribute("scUsername", claims.username());
        req.setAttribute("scRole", ROLE_LABEL.getOrDefault(top, ""));
        req.setAttribute("scStaff", roles.stream().anyMatch(STAFF::contains));
        req.setAttribute("scAdmin", roles.contains("ADMIN"));
        req.setAttribute("scManager", roles.contains("ADMIN") || roles.contains("CITY_MANAGER"));
        req.setAttribute("scSupervisor", roles.contains("ADMIN") || roles.contains("CITY_MANAGER") || roles.contains("SUPERVISOR"));
    }
}