package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class StreetlightPage {
    private final long total;
    private final int page;
    private final int size;
    private final List<StreetlightView> items;
}
