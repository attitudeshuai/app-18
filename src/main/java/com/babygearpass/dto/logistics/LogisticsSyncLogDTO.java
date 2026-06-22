package com.babygearpass.dto.logistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsSyncLogDTO {

    private Long id;
    private Long logisticsId;
    private String trackingNumber;
    private String expressCompanyCode;
    private String syncType;
    private Long operatorId;
    private Boolean isSuccess;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime syncStartTime;
    private LocalDateTime syncEndTime;
    private LocalDateTime createdAt;
}
