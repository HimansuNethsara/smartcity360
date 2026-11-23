package lk.jiat.smartcity360.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "city_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "createdBy")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CityPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", nullable = false, length = 80, unique = true)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    /** e.g. IF noise > 70 AND zone = "RESIDENTIAL" THEN FINE 5000 - parsed in Phase 4E. */
    @Lob
    @Column(name = "dsl_text", nullable = false)
    private String dslText;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}