package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PolicyView {
    private final Long id;
    private final String name;
    private final String description;
    private final String dslText;
    private final String canonicalText;
    private final boolean valid;
    private final String parseError;
    private final boolean enabled;
    private final Integer version;
    private final String createdBy;
    private final String updatedAt;
    private final AstNodeView ast;
}