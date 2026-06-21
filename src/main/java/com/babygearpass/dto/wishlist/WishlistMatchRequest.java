package com.babygearpass.dto.wishlist;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistMatchRequest {

    @NotNull
    private Long wishlistId;

    private Long gearItemId;

    private String message;
}
