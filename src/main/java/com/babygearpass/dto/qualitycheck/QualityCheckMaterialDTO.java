package com.babygearpass.dto.qualitycheck;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QualityCheckMaterialDTO {

    private Long id;
    private String materialType;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String description;
    private LocalDateTime createdAt;
}
