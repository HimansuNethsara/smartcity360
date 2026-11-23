package lk.jiat.smartcity360.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_readings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "sensor")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(name = "value", nullable = false, precision = 10, scale = 3)
    private BigDecimal value;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;
}