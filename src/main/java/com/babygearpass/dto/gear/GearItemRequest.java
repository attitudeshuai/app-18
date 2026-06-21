package com.babygearpass.dto.gear;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GearItemRequest {

    @NotBlank
    private String title;

    @NotNull
    private Long categoryId;

    @NotBlank
    private String condition;

    private String brand;

    private String suitableAge;

    private String description;

    private String photos;

    @NotBlank
    private String priceType;

    private BigDecimal price;
}
