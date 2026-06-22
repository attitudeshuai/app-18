package com.babygearpass.dto.logistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsManualUpdateRequest {

    private String currentStatus;

    private String currentLocation;

    private LocalDateTime estimatedDeliveryTime;

    private String newStatusCode;

    private String newStatusName;

    private String newLocation;

    private String newDescription;

    private LocalDateTime newOccurredAt;

    private String remark;
}
