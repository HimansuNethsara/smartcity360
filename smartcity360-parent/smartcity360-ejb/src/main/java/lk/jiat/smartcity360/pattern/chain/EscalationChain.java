package lk.jiat.smartcity360.pattern.chain;

import java.util.ArrayList;
import java.util.Optional;

public class EscalationChain {

    private final ComplaintHandler head;

    private EscalationChain(ComplaintHandler head) {
        this.head = head;
    }

    public static EscalationChain standard() {
        StaffHandler staff = new StaffHandler();
        staff.setNext(new SupervisorHandler()).setNext(new CityManagerHandler());
        return new EscalationChain(staff);
    }

    public Optional<RoutingDecision> route(RoutingRequest request) {
        return head.handle(request, new ArrayList<>());
    }

    public String describe() {
        StringBuilder sb = new StringBuilder();
        for (ComplaintHandler h = head; h != null; h = h.getNext()) {
            if (sb.length() > 0) {
                sb.append('>');
            }
            sb.append(h.getLevel());
        }
        return sb.toString();
    }
}
