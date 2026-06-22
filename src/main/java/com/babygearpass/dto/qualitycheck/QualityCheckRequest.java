package com.babygearpass.dto.qualitycheck;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QualityCheckRequest {

    @NotNull
    private Long gearItemId;

    private String remark;

    private List<QualityCheckMaterialRequest> materials;
}
