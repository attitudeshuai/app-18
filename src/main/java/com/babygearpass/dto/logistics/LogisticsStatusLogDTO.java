package com.babygearpass.dto.logistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsStatusLogDTO {

    private Long id;
    private String statusCode;
    private String statusName;
    private String location;
    private String description;
    private LocalDateTime occurredAt;
    private Boolean isSignatureRequired;
    private String operatorName;
    private String operatorPhone;
}
