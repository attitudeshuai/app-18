package com.babygearpass.dto.wishlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;
    private Long userId;
    private String userName;
    private Long wishlistId;
    private Long wishlistMatchId;
    private Long qualityCheckId;
    private String type;
    private String title;
    private String content;
    private String status;
    private LocalDateTime createdAt;
}
