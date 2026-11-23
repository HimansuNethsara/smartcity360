package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StreamAuditView {
    private final Long id;
    private final String layersApplied;
    private final Integer rawBytes;
    private final Integer finalBytes;
    private final String processedAt;
}