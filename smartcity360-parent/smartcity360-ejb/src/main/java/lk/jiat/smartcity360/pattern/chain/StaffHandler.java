package lk.jiat.smartcity360.pattern.chain;

import lk.jiat.smartcity360.domain.enums.ComplaintLevel;

import java.util.List;

public class StaffHandler extends ComplaintHandler {

    public StaffHandler() {
        super(ComplaintLevel.STAFF);
    }

    @Override
    protected RoutingDecision accept(RoutingRequest r, List<String> trace) {
        return new RoutingDecision(getLevel(), r.reason(), r.staffTimeoutMin(),
                "Assigned to front-line staff", trace);
    }
}
