package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChangedZone {
    private final Long id;
    private final String code;
    private final String name;
    private final String type;
    private final String power;
}