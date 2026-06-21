package com.babygearpass.dto.gear;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GearItemStatusRequest {

    @NotBlank
    private String status;
}
