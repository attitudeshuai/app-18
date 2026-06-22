package com.babygearpass.dto.logistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpressTrackResult {

    private boolean success;

    private String trackingNumber;

    private String expressCompanyCode;

    private String expressCompanyName;

    private String status;

    private String statusText;

    private String currentLocation;

    private LocalDateTime estimatedDeliveryTime;

    private LocalDateTime actualDeliveryTime;

    private String errorCode;

    private String errorMessage;

    private String rawResponse;

    private List<TrackItem> tracks = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackItem {
        private String statusCode;
        private String statusName;
        private String location;
        private String description;
        private LocalDateTime occurredAt;
        private String operatorName;
        private String operatorPhone;
        private Boolean isSignatureRequired;
    }

    public static ExpressTrackResult error(String errorCode, String errorMessage) {
        ExpressTrackResult result = new ExpressTrackResult();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
