package com.babygearpass.dto.qualitycheck;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QualityCheckReviewRequest {

    @NotBlank
    private String status;

    private String rejectReason;

    private LocalDateTime supplementDeadline;

    private Integer qualityScore;

    private String remark;
}
