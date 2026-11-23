package lk.jiat.smartcity360.domain;

import jakarta.persistence.*;
import lk.jiat.smartcity360.domain.enums.ComplaintLevel;
import lk.jiat.smartcity360.domain.enums.ComplaintSeverity;
import lombok.*;

@Entity
@Table(name = "escalation_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EscalationPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Short id;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10, unique = true)
    private ComplaintSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "start_level", nullable = false, length = 15)
    private ComplaintLevel startLevel;

    @Column(name = "staff_timeout_min", nullable = false)
    private Short staffTimeoutMin;

    @Column(name = "supervisor_timeout_min", nullable = false)
    private Short supervisorTimeoutMin;
}