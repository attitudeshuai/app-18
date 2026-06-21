package com.babygearpass.dto.gear;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GearItemDTO {

    private Long id;
    private Long ownerId;
    private String ownerName;
    private String title;
    private Long categoryId;
    private String categoryName;
    private String condition;
    private String brand;
    private String suitableAge;
    private String description;
    private String photos;
    private String status;
    private String priceType;
    private BigDecimal price;
    private LocalDateTime createdAt;
}
