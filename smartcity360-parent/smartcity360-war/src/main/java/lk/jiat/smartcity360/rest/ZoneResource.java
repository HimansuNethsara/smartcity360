package lk.jiat.smartcity360.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lk.jiat.smartcity360.dto.PowerChangeResult;
import lk.jiat.smartcity360.dto.ZoneNodeView;
import lk.jiat.smartcity360.dto.ZoneTreeView;
import lk.jiat.smartcity360.service.ZoneService;

@Path("/zones")
@Roles({"ADMIN", "CITY_MANAGER", "SUPERVISOR", "STAFF"})
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class ZoneResource {

    @Inject
    private ZoneService zoneService;

    @GET
    @Path("/tree")
    public ZoneTreeView tree() {
        return zoneService.tree();
    }

    @GET
    @Path("/{id}/usage")
    public ZoneNodeView usage(@PathParam("id") Long id) {
        return zoneService.usage(id);
    }

    @POST
    @Path("/{id}/shutdown")
    @Roles({"ADMIN", "CITY_MANAGER"})
    public PowerChangeResult shutdown(@PathParam("id") Long id) {
        return zoneService.shutdown(id);
    }

    @POST
    @Path("/{id}/restore")
    @Roles({"ADMIN", "CITY_MANAGER"})
    public PowerChangeResult restore(@PathParam("id") Long id) {
        return zoneService.restore(id);
    }
}