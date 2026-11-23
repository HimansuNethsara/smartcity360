package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AstNodeView {
    private final String label;
    private final List<AstNodeView> children;
}
