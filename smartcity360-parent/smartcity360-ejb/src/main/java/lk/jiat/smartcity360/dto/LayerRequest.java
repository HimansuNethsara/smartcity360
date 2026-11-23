package lk.jiat.smartcity360.dto;

import lk.jiat.smartcity360.domain.enums.LayerType;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LayerRequest {
    private LayerType layerType;
    @Min(1) @Max(20)
    private Integer layerOrder;
    @Size(max = 255)
    private String config;
    private Boolean enabled;
}
