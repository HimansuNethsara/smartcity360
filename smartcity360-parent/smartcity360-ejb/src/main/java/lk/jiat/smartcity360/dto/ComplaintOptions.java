package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ComplaintOptions {
    private final List<ZoneOption> citizens;
    private final List<ZoneOption> zones;
    private final List<EscalationPolicyView> policies;
}
