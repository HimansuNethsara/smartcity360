package lk.jiat.smartcity360.pattern.chain;

import lk.jiat.smartcity360.domain.enums.ComplaintLevel;

import java.util.List;
import java.util.Optional;

public abstract class ComplaintHandler {

    private final ComplaintLevel level;
    private ComplaintHandler next;

    protected ComplaintHandler(ComplaintLevel level) {
        this.level = level;
    }

    public ComplaintHandler setNext(ComplaintHandler next) {
        this.next = next;
        return next;
    }

    public final Optional<RoutingDecision> handle(RoutingRequest request, List<String> trace) {
        ComplaintLevel minimum = request.minimumLevel();
        if (minimum != null && level.ordinal() >= minimum.ordinal()) {
            trace.add(level + ": accepted");
            return Optional.of(accept(request, trace));
        }
        trace.add(level + ": passed on" + (minimum == null ? "" : " (needs " + minimum + " or higher)"));
        return next == null ? Optional.empty() : next.handle(request, trace);
    }

    protected abstract RoutingDecision accept(RoutingRequest request, List<String> trace);

    public final ComplaintLevel getLevel() {
        return level;
    }

    public final ComplaintHandler getNext() {
        return next;
    }
}
