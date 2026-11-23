package lk.jiat.smartcity360.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lk.jiat.smartcity360.dto.*;
import lk.jiat.smartcity360.service.PolicyAdminService;
import lk.jiat.smartcity360.service.PolicyEvaluationService;

import java.util.List;

@Path("/policies")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Roles({"ADMIN", "CITY_MANAGER", "SUPERVISOR", "STAFF"})
public class PolicyResource {

    @Inject
    private PolicyAdminService adminService;
    @Inject
    private PolicyEvaluationService evaluationService;
    @Context
    private SecurityContext security;

    @GET
    public List<PolicyView> list() {
        return adminService.list();
    }

    @GET
    @Path("/evaluations")
    public List<EvaluationView> recentAll(@QueryParam("limit") @DefaultValue("20") int limit) {
        return evaluationService.recent(null, limit);
    }

    @GET
    @Path("/{id}")
    public PolicyView get(@PathParam("id") Long id) {
        return adminService.get(id);
    }

    @GET
    @Path("/{id}/versions")
    public List<PolicyVersionView> versions(@PathParam("id") Long id) {
        return adminService.versions(id);
    }

    @POST
    @Path("/validate")
    public ValidationView validate(ValidateRequest request) {
        return adminService.validate(request);
    }

    @POST
    @Roles({"ADMIN", "CITY_MANAGER"})
    public Response create(PolicyRequest request) {
        request.setActorId(Caller.of(security).id());
        return Response.status(Response.Status.CREATED).entity(adminService.create(request)).build();
    }

    @PUT
    @Path("/{id}")
    @Roles({"ADMIN", "CITY_MANAGER"})
    public PolicyView update(@PathParam("id") Long id, PolicyRequest request) {
        request.setActorId(Caller.of(security).id());
        return adminService.update(id, request);
    }

    @PUT
    @Path("/{id}/enabled")
    @Roles({"ADMIN", "CITY_MANAGER"})
    public PolicyView setEnabled(@PathParam("id") Long id, ToggleRequest request) {
        return adminService.setEnabled(id, request);
    }

    @POST
    @Path("/{id}/rollback")
    @Roles({"ADMIN", "CITY_MANAGER"})
    public PolicyView rollback(@PathParam("id") Long id, RollbackRequest request) {
        request.setActorId(Caller.of(security).id());
        return adminService.rollback(id, request);
    }

    @POST
    @Path("/{id}/evaluate")
    @Roles({"ADMIN", "CITY_MANAGER", "SUPERVISOR"})
    public EvaluationView evaluate(@PathParam("id") Long id, EvaluateRequest request) {
        return evaluationService.evaluate(id, request == null ? new EvaluateRequest() : request);
    }

    @GET
    @Path("/{id}/evaluations")
    public List<EvaluationView> recent(@PathParam("id") Long id, @QueryParam("limit") @DefaultValue("20") int limit) {
        return evaluationService.recent(id, limit);
    }

    @POST
    @Path("/run-all")
    @Roles({"ADMIN", "CITY_MANAGER", "SUPERVISOR"})
    public RunSummary runAll() {
        return evaluationService.runAllEnabled();
    }
}
