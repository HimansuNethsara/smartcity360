package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ValidationView {
    private final boolean valid;
    private final String canonicalText;
    private final String error;
    private final int position;
    private final AstNodeView ast;
}
