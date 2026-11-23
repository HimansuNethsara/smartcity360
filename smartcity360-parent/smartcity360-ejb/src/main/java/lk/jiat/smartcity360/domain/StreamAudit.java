package lk.jiat.smartcity360.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stream_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "meter")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StreamAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meter_id", nullable = false)
    private EnergyMeter meter;

    @Column(name = "layers_applied", nullable = false, length = 120)
    private String layersApplied;

    @Column(name = "raw_bytes", nullable = false)
    private Integer rawBytes;

    @Column(name = "final_bytes", nullable = false)
    private Integer finalBytes;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;
}