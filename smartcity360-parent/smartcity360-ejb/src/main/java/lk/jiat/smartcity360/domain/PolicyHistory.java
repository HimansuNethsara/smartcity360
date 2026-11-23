package lk.jiat.smartcity360.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "policy_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"policy", "changedBy"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PolicyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private CityPolicy policy;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Lob
    @Column(name = "dsl_text", nullable = false)
    private String dslText;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Column(name = "changed_at", insertable = false, updatable = false)
    private LocalDateTime changedAt;
}