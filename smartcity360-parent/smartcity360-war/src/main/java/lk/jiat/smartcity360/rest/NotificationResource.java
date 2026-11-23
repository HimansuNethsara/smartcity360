package lk.jiat.smartcity360.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lk.jiat.smartcity360.dto.NotificationView;
import lk.jiat.smartcity360.service.NotificationService;

import java.util.List;
import java.util.Map;

@Path("/notifications")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Roles({"ADMIN", "CITY_MANAGER", "SUPERVISOR", "STAFF", "CITIZEN"})
public class NotificationResource {

    @Inject
    private NotificationService notificationService;
    @Context
    private SecurityContext security;

    @GET
    public List<NotificationView> list(@QueryParam("unread") boolean unread) {
        return notificationService.list(me(), unread);
    }

    @GET
    @Path("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("unread", notificationService.unreadCount(me()));
    }

    @POST
    @Path("/{id}/read")
    public Response markRead(@PathParam("id") Long id) {
        notificationService.markRead(id, me());
        return Response.noContent().build();
    }

    @POST
    @Path("/read-all")
    public Map<String, Integer> readAll() {
        return Map.of("changed", notificationService.markAllRead(me()));
    }

    private Long me() {
        return Caller.of(security).id();
    }
}
