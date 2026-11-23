package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PoolStats {
    private final int poolSize;
    private final long requests;
    private final long hits;
    private final long created;
}
