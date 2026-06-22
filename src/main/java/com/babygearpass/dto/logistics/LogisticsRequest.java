package com.babygearpass.dto.logistics;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsRequest {

    @NotNull
    private Long handoverId;

    @NotBlank
    private String trackingNumber;

    private String expressCompanyCode;

    private String expressCompanyName;

    private String senderName;

    private String senderPhone;

    private String senderAddress;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private String remark;
}
