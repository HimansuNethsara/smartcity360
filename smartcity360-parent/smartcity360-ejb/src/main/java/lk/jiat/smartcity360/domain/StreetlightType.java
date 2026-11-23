package lk.jiat.smartcity360.domain;

import jakarta.persistence.*;
import lk.jiat.smartcity360.domain.enums.LampType;
import lombok.*;

@Entity
@Table(name = "streetlight_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StreetlightType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "type_code", nullable = false, length = 30, unique = true)
    private String typeCode;

    @Column(name = "model", nullable = false, length = 60)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "lamp_type", nullable = false, length = 10)
    private LampType lampType;

    @Column(name = "wattage", nullable = false)
    private Integer wattage;

    @Column(name = "max_lumens", nullable = false)
    private Integer maxLumens;

    @Column(name = "color_temp_k", nullable = false)
    private Integer colorTempK;

    @Column(name = "firmware", nullable = false, length = 20)
    private String firmware;
}