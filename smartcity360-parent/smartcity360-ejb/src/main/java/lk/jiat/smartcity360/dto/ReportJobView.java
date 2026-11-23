package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportJobView {
    private Long id;
    private String reportType;
    private String status;
    private String paramsJson;
    private String errorMessage;
    private String requestedAt;
    private String completedAt;
    private String resultJson;
}
