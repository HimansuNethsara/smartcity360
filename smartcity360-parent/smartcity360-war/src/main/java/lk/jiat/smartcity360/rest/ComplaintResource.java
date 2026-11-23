package lk.jiat.smartcity360.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lk.jiat.smartcity360.domain.enums.ComplaintLevel;
import lk.jiat.smartcity360.domain.enums.ComplaintStatus;
import lk.jiat.smartcity360.dto.*;
import lk.jiat.smartcity360.service.ComplaintLookupService;
import lk.jiat.smartcity360.service.ComplaintService;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;

import java.util.List;

/**
 * PART C REST: /smartcity360/api/complaints/*
 * Citizens raise complaints and see only their own. Staff, supervisors, city managers and admins see all
 * and work on them; the timeout check is for city managers and admins, the demo helper for admins.
 */
@Path("/complaints")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Roles({"ADMIN", "CITY_MANAGER", "SUPERVISOR", "STAFF", "CITIZEN"})
public class ComplaintResource {

    @Inject
    private ComplaintService complaintService;
    @Inject
    private ComplaintLookupService lookupService;
    @Context
    private SecurityContext security;

    @GET
    public List<ComplaintView> list(@QueryParam("status") String status, @QueryParam("level") String level) {
        return complaintService.list(parse(ComplaintStatus.class, status), parse(ComplaintLevel.class, level),
                onlyOwn());
    }

    @GET
    @Path("/options")
    public ComplaintOptions options() {
        ComplaintOptions o = lookupService.options();
        if (Caller.isStaff(Caller.of(security))) {
            return o;
        }
        return new ComplaintOptions(List.of(), o.getZones(), o.getPolicies());
    }

    @GET
    @Path("/{id}")
    public ComplaintView get(@PathParam("id") Long id) {
        return complaintService.get(id, onlyOwn());
    }

    @POST
    public Response submit(ComplaintRequest request) {
        AuthenticatedUser user = Caller.of(security);
        if (!Caller.isStaff(user)) {
            request.setCitizenId(user.id());          // a citizen always files as themselves
        }
        return Response.status(Response.Status.CREATED).entity(complaintService.submit(request)).build();
    }

    @POST
    @Path("/{id}/escalate")
    @Roles({"ADMIN", "CITY_MANAGER", "SUPERVISOR", "STAFF"})
    public ComplaintView escalate(@PathParam("id") Long id, EscalateRequest request) {
        return complaintService.escalate(id, request);
    }

    @POST
    @Path("/{id}/status")
    @Roles({"ADMIN", "CITY_MANAGER", "SUPERVISOR", "STAFF"})
    public ComplaintView status(@PathParam("id") Long id, StatusRequest request) {
        return complaintService.changeStatus(id, request);
    }

    @POST
    @Path("/process-timeouts")
    @Roles({"ADMIN", "CITY_MANAGER"})
    public OverdueResult processTimeouts() {
        return complaintService.processTimeouts();
    }

    @POST
    @Path("/{id}/make-overdue")
    @Roles({"ADMIN"})
    public ComplaintView makeOverdue(@PathParam("id") Long id) {
        return complaintService.makeOverdue(id);
    }

    private Long onlyOwn() {
        AuthenticatedUser user = Caller.of(security);
        return Caller.isStaff(user) ? null : user.id();
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Unknown " + type.getSimpleName() + " '" + value + "'");
        }
    }
}
