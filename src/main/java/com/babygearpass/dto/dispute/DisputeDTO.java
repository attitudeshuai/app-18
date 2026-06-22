package com.babygearpass.dto.dispute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisputeDTO {

    private Long id;
    private Long handoverId;
    private Long initiatorId;
    private String initiatorName;
    private String status;
    private String disputeType;
    private String description;
    private Long arbitratorId;
    private String arbitratorName;
    private String arbitrationResult;
    private String arbitrationOpinion;
    private Integer giverCreditChange;
    private Integer receiverCreditChange;
    private String pointsHandleResult;
    private LocalDateTime arbitratedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DisputeEvidenceDTO> evidences;
    private List<ArbitrationRecordDTO> arbitrationRecords;
}
