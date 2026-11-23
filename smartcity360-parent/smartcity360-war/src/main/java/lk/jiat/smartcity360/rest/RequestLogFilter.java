package lk.jiat.smartcity360.rest;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import lk.jiat.smartcity360.service.AuditService;

import java.io.IOException;
import java.security.Principal;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class RequestLogFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger ACCESS = Logger.getLogger("lk.jiat.smartcity360.access");
    private static final ThreadLocal<String> CURRENT_ID = new ThreadLocal<>();
    private static final String START = "sc360.start";
    private static final String ID = "sc360.requestId";

    @Context
    private HttpServletRequest servlet;
    @Inject
    private AuditService auditService;

    public static String currentId() {
        return CURRENT_ID.get();
    }

    @Override
    public void filter(ContainerRequestContext req) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        CURRENT_ID.set(id);
        req.setProperty(ID, id);
        req.setProperty(START, System.nanoTime());
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        try {
            String id = (String) req.getProperty(ID);
            Object start = req.getProperty(START);
            long ms = start instanceof Long s ? (System.nanoTime() - s) / 1_000_000 : -1;
            String method = req.getMethod();
            String path = "/" + req.getUriInfo().getPath();
            int status = res.getStatus();
            Long userId = userId(req);

            if (id != null) {
                res.getHeaders().putSingle("X-Request-Id", id);
            }
            boolean write = !"GET".equals(method) && !"HEAD".equals(method) && !"OPTIONS".equals(method);
            Level level = status >= 500 ? Level.SEVERE : status >= 400 ? Level.WARNING : write ? Level.INFO : Level.FINE;
            if (ACCESS.isLoggable(level)) {
                String who = userId != null ? "user=" + userId : "anonymous";
                String from = path.startsWith("/auth/") && servlet != null ? " ip=" + servlet.getRemoteAddr() : "";
                ACCESS.log(level, "[" + id + "] " + method + " /api" + path + " " + status + " " + ms + "ms " + who + from);
            }
            if (write && status >= 200 && status < 300 && userId != null && !path.startsWith("/users")
                    && !path.startsWith("/notifications")) {
                audit(userId, method, path, status, ms, id);
            }
        } finally {
            CURRENT_ID.remove();
        }
    }

    private void audit(long userId, String method, String path, int status, long ms, String id) {
        try {
            String pattern = path.replaceAll("/\\d+", "/{id}");
            String[] parts = path.split("/");
            String entity = parts.length > 1 ? parts[1].toUpperCase() : "API";
            Long entityId = null;
            for (String p : parts) {
                if (p.matches("\\d{1,18}")) {
                    entityId = Long.parseLong(p);
                    break;
                }
            }
            String detail = "{\"requestId\":\"" + id + "\",\"path\":\"" + path.replaceAll("[^A-Za-z0-9/_.-]", "") + "\","
                    + "\"status\":" + status + ",\"ms\":" + ms + "}";
            auditService.record(userId, method + " " + pattern, entity, entityId, detail);
        } catch (RuntimeException e) {
            ACCESS.log(Level.WARNING, "[" + id + "] audit row not written", e);
        }
    }

    private static Long userId(ContainerRequestContext req) {
        Principal p = req.getSecurityContext() == null ? null : req.getSecurityContext().getUserPrincipal();
        return p instanceof AuthenticatedUser au ? au.id() : null;
    }
}
