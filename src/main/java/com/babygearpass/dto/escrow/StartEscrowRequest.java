package com.babygearpass.dto.escrow;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartEscrowRequest {

    @NotNull
    @Min(0)
    private Integer points;
}
