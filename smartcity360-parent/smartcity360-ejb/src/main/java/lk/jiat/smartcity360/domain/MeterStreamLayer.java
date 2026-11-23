package lk.jiat.smartcity360.domain;

import jakarta.persistence.*;
import lk.jiat.smartcity360.domain.enums.LayerType;
import lombok.*;

@Entity
@Table(name = "meter_stream_layers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "meter")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MeterStreamLayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meter_id", nullable = false)
    private EnergyMeter meter;

    @Enumerated(EnumType.STRING)
    @Column(name = "layer_type", nullable = false, length = 20)
    private LayerType layerType;

    @Column(name = "layer_order", nullable = false)
    private Short layerOrder;

    @Column(name = "config", length = 255)
    private String config;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;
}