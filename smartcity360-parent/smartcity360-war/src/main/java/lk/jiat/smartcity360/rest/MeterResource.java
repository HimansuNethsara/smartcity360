package lk.jiat.smartcity360.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.jiat.smartcity360.dto.*;
import lk.jiat.smartcity360.service.MeterAdminService;
import lk.jiat.smartcity360.service.MeterStreamService;

import java.util.List;

@Path("/meters")
@Roles({"ADMIN", "CITY_MANAGER", "SUPERVISOR", "STAFF"})
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MeterResource {

    @Inject
    private MeterAdminService adminService;
    @Inject
    private MeterStreamService streamService;

    @GET
    public List<MeterView> list() {
        return adminService.listMeters();
    }

    @GET
    @Path("/building-zones")
    public List<ZoneOption> buildingZones() {
        return adminService.buildingZones();
    }

    @GET
    @Path("/{id}")
    public MeterView get(@PathParam("id") Long id) {
        return adminService.getMeter(id);
    }

    @POST
    @Roles({"ADMIN"})
    public Response create(MeterRequest request) {
        return Response.status(Response.Status.CREATED).entity(adminService.createMeter(request)).build();
    }

    @POST
    @Path("/{id}/layers")
    @Roles({"ADMIN", "CITY_MANAGER"})
    public Response addLayer(@PathParam("id") Long id, LayerRequest request) {
        return Response.status(Response.Status.CREATED).entity(adminService.addLayer(id, request)).build();
    }

    @PUT
    @Path("/{id}/layers/{layerId}")
    @Roles({"ADMIN", "CITY_MANAGER"})
    public LayerView updateLayer(@PathParam("id") Long id, @PathParam("layerId") Long layerId, LayerRequest request) {
        return adminService.updateLayer(id, layerId, request);
    }

    @DELETE
    @Path("/{id}/layers/{layerId}")
    @Roles({"ADMIN", "CITY_MANAGER"})
    public Response deleteLayer(@PathParam("id") Long id, @PathParam("layerId") Long layerId) {
        adminService.deleteLayer(id, layerId);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/transmit")
    @Roles({"ADMIN", "CITY_MANAGER", "SUPERVISOR", "STAFF"})
    public StreamResult transmit(@PathParam("id") Long id) {
        return streamService.transmit(id);
    }

    @GET
    @Path("/{id}/audit")
    public List<StreamAuditView> audit(@PathParam("id") Long id, @QueryParam("limit") @DefaultValue("20") int limit) {
        return streamService.audit(id, limit);
    }
}