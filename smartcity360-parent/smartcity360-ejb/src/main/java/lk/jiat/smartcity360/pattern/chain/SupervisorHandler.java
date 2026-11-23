package lk.jiat.smartcity360.pattern.chain;

import lk.jiat.smartcity360.domain.enums.ComplaintLevel;

import java.util.List;

public class SupervisorHandler extends ComplaintHandler {

    public SupervisorHandler() {
        super(ComplaintLevel.SUPERVISOR);
    }

    @Override
    protected RoutingDecision accept(RoutingRequest r, List<String> trace) {
        return new RoutingDecision(getLevel(), r.reason(), r.supervisorTimeoutMin(),
                "Escalated to a supervisor", trace);
    }
}
