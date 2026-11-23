package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EvaluationView {
    private final Long id;
    private final Long policyId;
    private final String policyName;
    private final String zoneName;
    private final String inputJson;
    private final String outcome;
    private final String actionResult;
    private final String evaluatedAt;
}
