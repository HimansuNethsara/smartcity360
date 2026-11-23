package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PolicyVersionView {
    private final int versionNo;
    private final String dslText;
    private final String changedBy;
    private final String changedAt;
    private final boolean current;
}
