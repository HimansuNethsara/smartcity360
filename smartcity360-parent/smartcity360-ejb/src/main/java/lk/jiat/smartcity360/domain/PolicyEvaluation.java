package lk.jiat.smartcity360.domain;

import jakarta.persistence.*;
import lk.jiat.smartcity360.domain.enums.PolicyOutcome;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "policy_evaluations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"policy", "zone"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PolicyEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private CityPolicy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @Column(name = "input_json", nullable = false)
    private String inputJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 10)
    private PolicyOutcome outcome;

    @Column(name = "action_result", length = 255)
    private String actionResult;

    @Column(name = "evaluated_at", nullable = false)
    private LocalDateTime evaluatedAt;
}