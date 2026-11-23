package lk.jiat.smartcity360.domain;

import jakarta.persistence.*;
import lk.jiat.smartcity360.domain.enums.LandUse;
import lk.jiat.smartcity360.domain.enums.PowerStatus;
import lk.jiat.smartcity360.domain.enums.ZoneType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "zones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"parent", "children"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Zone parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Zone> children = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "zone_type", nullable = false, length = 20)
    private ZoneType zoneType;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", nullable = false, length = 30, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "land_use", nullable = false, length = 20)
    private LandUse landUse;

    @Enumerated(EnumType.STRING)
    @Column(name = "power_status", nullable = false, length = 10)
    @Builder.Default
    private PowerStatus powerStatus = PowerStatus.ON;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isLeaf() {
        return zoneType == ZoneType.BUILDING;
    }
}