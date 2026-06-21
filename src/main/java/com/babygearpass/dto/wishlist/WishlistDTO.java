package com.babygearpass.dto.wishlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDTO {

    private Long id;
    private Long userId;
    private String userName;
    private String title;
    private Long categoryId;
    private String categoryName;
    private String expectedCondition;
    private String city;
    private String acceptableWearLevel;
    private String description;
    private String status;
    private Long matchedGearItemId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
