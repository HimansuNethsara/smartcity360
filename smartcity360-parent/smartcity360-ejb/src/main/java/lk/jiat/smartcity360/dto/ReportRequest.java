package lk.jiat.smartcity360.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportRequest {
    private String reportType;
    private Integer hours;
}
