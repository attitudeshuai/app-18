package com.babygearpass.dto.dispute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisputeRequest {

    @NotNull
    private Long handoverId;

    @NotBlank
    private String disputeType;

    private String description;
}
