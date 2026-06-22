package com.babygearpass.dto.qualitycheck;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QualityCheckMaterialRequest {

    @NotBlank
    private String materialType;

    @NotBlank
    private String fileName;

    @NotBlank
    private String fileUrl;

    private Long fileSize;

    private String description;
}
