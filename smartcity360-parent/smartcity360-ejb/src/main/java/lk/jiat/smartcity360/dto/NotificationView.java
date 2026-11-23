package lk.jiat.smartcity360.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationView {
    private Long id;
    private String type;
    private String title;
    private String message;
    private boolean read;
    private String createdAt;
}
