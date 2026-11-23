package lk.jiat.smartcity360.pattern.chain;

import lk.jiat.smartcity360.domain.enums.ComplaintLevel;
import lk.jiat.smartcity360.domain.enums.EscalationReason;

import java.util.List;

public record RoutingDecision(ComplaintLevel level, EscalationReason reason, Integer timeoutMinutes,
                              String note, List<String> trace) {
}
