package lk.jiat.smartcity360.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StreamResult {
    private final String meterCode;
    private final String chain;
    private final int layerCount;
    private final int rawBytes;
    private final int finalBytes;

    private final double sizePercent;

    private final boolean roundTripOk;

    private final String wirePreviewHex;
    private final Long auditId;
    private final String processedAt;
}