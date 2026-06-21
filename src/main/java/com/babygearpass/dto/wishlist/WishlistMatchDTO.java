package com.babygearpass.dto.wishlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistMatchDTO {

    private Long id;
    private Long wishlistId;
    private String wishlistTitle;
    private Long providerId;
    private String providerName;
    private Long gearItemId;
    private String gearItemTitle;
    private String status;
    private String message;
    private Boolean isAccepted;
    private LocalDateTime createdAt;
}
