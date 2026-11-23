package lk.jiat.smartcity360.pattern.chain;

import lk.jiat.smartcity360.domain.enums.ComplaintLevel;
import lk.jiat.smartcity360.domain.enums.ComplaintSeverity;
import lk.jiat.smartcity360.domain.enums.EscalationReason;

public record RoutingRequest(ComplaintSeverity severity, ComplaintLevel currentLevel, EscalationReason reason,
                             ComplaintLevel policyStartLevel, int staffTimeoutMin, int supervisorTimeoutMin) {

    public static RoutingRequest forNewComplaint(ComplaintSeverity severity, ComplaintLevel startLevel,
                                                 int staffTimeoutMin, int supervisorTimeoutMin) {
        EscalationReason why = startLevel == ComplaintLevel.STAFF ? EscalationReason.INITIAL : EscalationReason.SEVERITY;
        return new RoutingRequest(severity, null, why, startLevel, staffTimeoutMin, supervisorTimeoutMin);
    }

    public static RoutingRequest forEscalation(ComplaintSeverity severity, ComplaintLevel from, EscalationReason reason,
                                               int staffTimeoutMin, int supervisorTimeoutMin) {
        return new RoutingRequest(severity, from, reason, null, staffTimeoutMin, supervisorTimeoutMin);
    }

    public ComplaintLevel minimumLevel() {
        if (currentLevel == null) {
            return policyStartLevel;
        }
        ComplaintLevel[] all = ComplaintLevel.values();
        int next = currentLevel.ordinal() + 1;
        return next < all.length ? all[next] : null;
    }
}
