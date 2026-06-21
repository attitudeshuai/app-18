package com.babygearpass.dto.wishlist;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistMatchStatusRequest {

    @NotBlank
    private String status;

    private Boolean isAccepted;
}
