package lk.jiat.smartcity360.domain;

import jakarta.persistence.*;
import lk.jiat.smartcity360.domain.enums.ComplaintLevel;
import lk.jiat.smartcity360.domain.enums.EscalationReason;
import lombok.*;

import java.time.LocalDateTime;

/** complaint_escalations: the traceability trail the Chain writes on every handoff (Part C Q3). */
@Entity
@Table(name = "complaint_escalations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"complaint", "handledBy"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ComplaintEscalation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_level", length = 15)
    private ComplaintLevel fromLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_level", nullable = false, length = 15)
    private ComplaintLevel toLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by")
    private User handledBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 10)
    private EscalationReason reason;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}