package com.babygearpass.dto.dispute;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisputeEvidenceRequest {

    @NotBlank
    private String type;

    @NotBlank
    private String title;

    private String description;

    private String fileUrl;
}
