package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ComplaintView {
    private final Long id;
    private final String citizenName;
    private final String zoneName;
    private final Long streetlightId;
    private final String category;
    private final String title;
    private final String description;
    private final String severity;
    private final String status;
    private final String currentLevel;
    private final String assignedTo;
    private final String escalateAt;
    private final Long minutesLeft;
    private final String createdAt;
    private final String resolvedAt;
    private final List<EscalationEntryView> history;
    private final List<String> trace;
}
