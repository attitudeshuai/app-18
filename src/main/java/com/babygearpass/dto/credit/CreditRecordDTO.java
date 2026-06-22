package com.babygearpass.dto.credit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditRecordDTO {

    private Long id;
    private Long userId;
    private String username;
    private Long relatedHandoverId;
    private Long relatedDisputeId;
    private String type;
    private Integer scoreChange;
    private Integer beforeScore;
    private Integer afterScore;
    private String reason;
    private Long operatorId;
    private LocalDateTime createdAt;
}
