package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EscalationPolicyView {
    private final String severity;
    private final String startLevel;
    private final int staffTimeoutMin;
    private final int supervisorTimeoutMin;
}
