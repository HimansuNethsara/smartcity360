package lk.jiat.smartcity360.domain;

import jakarta.persistence.*;
import lk.jiat.smartcity360.domain.enums.ComplaintCategory;
import lk.jiat.smartcity360.domain.enums.ComplaintLevel;
import lk.jiat.smartcity360.domain.enums.ComplaintSeverity;
import lk.jiat.smartcity360.domain.enums.ComplaintStatus;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"citizen", "zone", "streetlight", "assignedTo"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "streetlight_id")
    private Streetlight streetlight;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private ComplaintCategory category;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10)
    private ComplaintSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_level", nullable = false, length = 15)
    private ComplaintLevel currentLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "escalate_at")
    private LocalDateTime escalateAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;
}