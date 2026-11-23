package lk.jiat.smartcity360.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "meter_readings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "meter")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MeterReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meter_id", nullable = false)
    private EnergyMeter meter;

    @Column(name = "kwh", nullable = false, precision = 12, scale = 3)
    private BigDecimal kwh;

    @Column(name = "voltage", precision = 6, scale = 2)
    private BigDecimal voltage;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;
}