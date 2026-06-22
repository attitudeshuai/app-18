package com.babygearpass.dto.dispute;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArbitrationRequest {

    @NotBlank
    private String arbitrationResult;

    private String arbitrationOpinion;

    private Integer giverCreditChange;

    private Integer receiverCreditChange;

    private String pointsHandleResult;

    private String remark;
}
