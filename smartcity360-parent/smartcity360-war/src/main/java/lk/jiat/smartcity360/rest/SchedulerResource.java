package lk.jiat.smartcity360.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lk.jiat.smartcity360.dto.PollResult;
import lk.jiat.smartcity360.dto.RunSummary;
import lk.jiat.smartcity360.service.PolicySweepService;
import lk.jiat.smartcity360.service.SensorPollingService;

@Path("/scheduler")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Roles({"ADMIN"})
public class SchedulerResource {

    @Inject
    private SensorPollingService pollingService;
    @Inject
    private PolicySweepService sweepService;

    @POST
    @Path("/poll-now")
    public PollResult pollNow() {
        return pollingService.pollOnce();
    }

    @POST
    @Path("/sweep-now")
    public RunSummary sweepNow() {
        return sweepService.sweep();
    }
}
