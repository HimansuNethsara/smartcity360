package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OverdueResult {
    private final int escalated;
    private final List<Long> complaintIds;
}
