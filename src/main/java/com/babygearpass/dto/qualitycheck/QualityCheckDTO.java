package com.babygearpass.dto.qualitycheck;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QualityCheckDTO {

    private Long id;
    private Long gearItemId;
    private String gearItemTitle;
    private Long submitterId;
    private String submitterName;
    private Long reviewerId;
    private String reviewerName;
    private String status;
    private String rejectReason;
    private LocalDateTime supplementDeadline;
    private String remark;
    private Integer qualityScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime reviewedAt;
    private List<QualityCheckMaterialDTO> materials;
}
