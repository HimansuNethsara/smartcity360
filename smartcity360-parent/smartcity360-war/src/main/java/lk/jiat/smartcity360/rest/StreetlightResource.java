package lk.jiat.smartcity360.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lk.jiat.smartcity360.domain.enums.StreetlightStatus;
import lk.jiat.smartcity360.dto.*;
import lk.jiat.smartcity360.service.StreetlightService;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;

import java.util.List;

@Path("/streetlights")
@Roles({"ADMIN", "CITY_MANAGER", "SUPERVISOR", "STAFF"})
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StreetlightResource {

    @Inject
    private StreetlightService service;

    @GET
    public StreetlightPage page(@QueryParam("zone") Long zoneId, @QueryParam("status") String status,
                                @QueryParam("page") @DefaultValue("0") int page,
                                @QueryParam("size") @DefaultValue("20") int size) {
        StreetlightStatus st = null;
        if (status != null && !status.isBlank()) {
            try {
                st = StreetlightStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleException("Unknown status '" + status + "'");
            }
        }
        return service.page(zoneId, st, page, size);
    }

    @GET
    @Path("/summary")
    public StreetlightSummary summary() {
        return service.summary();
    }

    @GET
    @Path("/street-zones")
    public List<ZoneOption> streetZones() {
        return service.streetZones();
    }

    @GET
    @Path("/memory")
    public MemoryView memory(@QueryParam("count") @DefaultValue("10000") int count) {
        return service.memory(count);
    }

    @POST
    @Path("/generate")
    @Roles({"ADMIN"})
    public GenerateResult generate(GenerateRequest request) {
        return service.generate(request);
    }

    @DELETE
    @Path("/generated")
    @Roles({"ADMIN"})
    public GenerateResult deleteGenerated() {
        return service.deleteGenerated();
    }

    @PUT
    @Path("/{id}")
    @Roles({"ADMIN"})
    public StreetlightView update(@PathParam("id") Long id, UpdateLightRequest request) {
        return service.update(id, request);
    }

    @POST
    @Path("/pool/reset")
    @Roles({"ADMIN"})
    public PoolStats resetPool() {
        return service.resetPool();
    }
}
