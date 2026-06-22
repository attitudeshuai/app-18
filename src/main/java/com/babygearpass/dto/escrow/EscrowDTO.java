package com.babygearpass.dto.escrow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EscrowDTO {

    private Long handoverId;
    private String escrowStatus;
    private LocalDateTime escrowStartAt;
    private LocalDateTime escrowEndAt;
    private Integer frozenPoints;
    private Boolean confirmedByReceiver;
    private LocalDateTime confirmedAt;
    private Boolean hasDispute;
    private Boolean isFrozen;
    private Long remainingSeconds;
}
