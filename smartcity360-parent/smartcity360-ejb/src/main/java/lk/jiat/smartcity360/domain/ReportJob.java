package lk.jiat.smartcity360.domain;

import jakarta.persistence.*;
import lk.jiat.smartcity360.domain.enums.ReportStatus;
import lk.jiat.smartcity360.domain.enums.ReportType;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "requestedBy")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 25)
    private ReportType reportType;

    @Column(name = "params_json")
    private String paramsJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    @Builder.Default
    private ReportStatus status = ReportStatus.QUEUED;

    @Column(name = "result_json")
    private String resultJson;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "requested_at", insertable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}