package lk.jiat.smartcity360.dto;

import lk.jiat.smartcity360.domain.enums.LayerType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LayerView {
    private final Long id;
    private final LayerType layerType;
    private final Short layerOrder;
    private final String config;
    private final Boolean enabled;
}