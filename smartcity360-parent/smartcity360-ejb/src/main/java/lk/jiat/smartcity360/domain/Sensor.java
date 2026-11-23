package lk.jiat.smartcity360.domain;

import jakarta.persistence.*;
import lk.jiat.smartcity360.domain.enums.SensorStatus;
import lk.jiat.smartcity360.domain.enums.SensorType;
import lombok.*;

@Entity
@Table(name = "sensors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "sensor_code", nullable = false, length = 30, unique = true)
    private String sensorCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type", nullable = false, length = 20)
    private SensorType sensorType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(name = "unit", nullable = false, length = 10)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    @Builder.Default
    private SensorStatus status = SensorStatus.ACTIVE;
}