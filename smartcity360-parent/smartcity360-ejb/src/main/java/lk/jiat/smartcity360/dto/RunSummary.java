package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RunSummary {
    private final int zones;
    private final int policies;
    private final int evaluated;
    private final int violations;
    private final int errors;
    private final int skipped;
    private final List<EvaluationView> findings;
}
