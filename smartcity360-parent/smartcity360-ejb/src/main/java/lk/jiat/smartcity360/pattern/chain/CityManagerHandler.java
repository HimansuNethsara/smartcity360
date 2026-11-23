package lk.jiat.smartcity360.pattern.chain;

import lk.jiat.smartcity360.domain.enums.ComplaintLevel;

import java.util.List;

public class CityManagerHandler extends ComplaintHandler {

    public CityManagerHandler() {
        super(ComplaintLevel.CITY_MANAGER);
    }

    @Override
    protected RoutingDecision accept(RoutingRequest r, List<String> trace) {
        return new RoutingDecision(getLevel(), r.reason(), null,
                "Escalated to the city manager (end of the chain)", trace);
    }
}
