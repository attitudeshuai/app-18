package com.babygearpass.dto.handover;

import com.babygearpass.dto.logistics.LogisticsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandoverDTO {

    private Long id;
    private Long gearItemId;
    private String gearItemTitle;
    private Long giverId;
    private String giverName;
    private Long receiverId;
    private String receiverName;
    private Long qualityCheckId;
    private String qualityCheckStatus;
    private Integer qualityScore;
    private Boolean qualityCertified;
    private LocalDate handoverDate;
    private String location;
    private String status;
    private String note;
    private LocalDateTime createdAt;
    private String escrowStatus;
    private LocalDateTime escrowStartAt;
    private LocalDateTime escrowEndAt;
    private Integer frozenPoints;
    private Boolean confirmedByReceiver;
    private Boolean hasDispute;
    private Boolean isFrozen;
    private LogisticsDTO logistics;
}
