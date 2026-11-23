package lk.jiat.smartcity360.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import lk.jiat.smartcity360.service.DashboardService;

import java.util.Map;

@Path("/dashboard")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Roles({"ADMIN", "CITY_MANAGER", "SUPERVISOR", "STAFF", "CITIZEN"})
public class DashboardResource {

    @Inject
    private DashboardService dashboardService;
    @Context
    private SecurityContext security;

    @GET
    @Path("/summary")
    public Map<String, Object> summary() {
        var me = Caller.of(security);
        boolean staff = me.hasAnyRole("ADMIN", "CITY_MANAGER", "SUPERVISOR", "STAFF");
        return dashboardService.summary(me.id(), staff);
    }
}
