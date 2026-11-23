package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EscalationEntryView {
    private final Long id;
    private final String fromLevel;
    private final String toLevel;
    private final String handledBy;
    private final String reason;
    private final String note;
    private final String createdAt;
}
