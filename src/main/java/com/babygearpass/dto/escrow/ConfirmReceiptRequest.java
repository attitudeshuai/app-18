package com.babygearpass.dto.escrow;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmReceiptRequest {

    @NotNull
    private Boolean acceptQuality;

    private String remark;
}
