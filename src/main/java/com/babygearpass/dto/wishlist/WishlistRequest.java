package com.babygearpass.dto.wishlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistRequest {

    @NotBlank
    private String title;

    @NotNull
    private Long categoryId;

    @NotBlank
    private String expectedCondition;

    @NotBlank
    private String city;

    @NotBlank
    private String acceptableWearLevel;

    private String description;
}
