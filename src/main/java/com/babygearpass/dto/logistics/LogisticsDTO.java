package com.babygearpass.dto.logistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsDTO {

    private Long id;
    private Long handoverId;
    private String trackingNumber;
    private String expressCompanyCode;
    private String expressCompanyName;
    private String currentStatus;
    private String currentLocation;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime actualDeliveryTime;
    private String senderName;
    private String senderPhone;
    private String senderAddress;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private LocalDateTime lastSyncTime;
    private String syncStatus;
    private Integer syncFailCount;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LogisticsStatusLogDTO> statusLogs;
}
