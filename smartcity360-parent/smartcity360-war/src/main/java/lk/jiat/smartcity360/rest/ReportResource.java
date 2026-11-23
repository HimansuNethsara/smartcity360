package lk.jiat.smartcity360.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lk.jiat.smartcity360.domain.enums.ReportType;
import lk.jiat.smartcity360.dto.ReportJobView;
import lk.jiat.smartcity360.dto.ReportRequest;
import lk.jiat.smartcity360.service.ReportJobService;
import lk.jiat.smartcity360.service.ReportRunner;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;

import java.util.List;
import java.util.Map;

@Path("/reports")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Roles({"ADMIN", "CITY_MANAGER", "SUPERVISOR", "STAFF"})
public class ReportResource {

    @Inject
    private ReportJobService jobService;
    @Inject
    private ReportRunner runner;
    @Context
    private SecurityContext security;

    @POST
    public Response request(ReportRequest body) {
        if (body == null || body.getReportType() == null) {
            throw new BusinessRuleException("reportType is required");
        }
        ReportType type;
        try {
            type = ReportType.valueOf(body.getReportType());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Unknown reportType " + body.getReportType());
        }
        var me = Caller.of(security);
        if (type == ReportType.POLICY_VIOLATIONS && !me.hasAnyRole("ADMIN", "CITY_MANAGER", "SUPERVISOR")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("message", "Policy violation reports need supervisor access"))
                    .build();
        }
        ReportJobView job = jobService.submit(me.id(), type, body.getHours());   // committed when this returns
        runner.start(job.getId());                                               // then the worker starts
        return Response.accepted(job).build();
    }

    @GET
    public List<ReportJobView> mine() {
        return jobService.listOwn(Caller.of(security).id());
    }

    @GET
    @Path("/{id}")
    public ReportJobView one(@PathParam("id") Long id) {
        return jobService.getOwn(id, Caller.of(security).id());
    }
}
