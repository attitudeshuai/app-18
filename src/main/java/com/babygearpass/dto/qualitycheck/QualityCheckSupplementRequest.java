package com.babygearpass.dto.qualitycheck;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QualityCheckSupplementRequest {

    @NotEmpty
    @Valid
    private List<QualityCheckMaterialRequest> materials;

    private String remark;
}
