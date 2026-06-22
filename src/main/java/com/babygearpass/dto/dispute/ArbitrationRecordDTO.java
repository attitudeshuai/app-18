package com.babygearpass.dto.dispute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArbitrationRecordDTO {

    private Long id;
    private Long disputeId;
    private Long operatorId;
    private String operatorName;
    private String action;
    private String remark;
    private Boolean isPublic;
    private LocalDateTime createdAt;
}
