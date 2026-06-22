package com.babygearpass.dto.logistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpressCompanyDTO {

    private String code;
    private String name;
    private String officialWebsite;
    private String phone;
    private List<String> trackingNumberPatterns;

    public boolean matchesTrackingNumber(String trackingNumber) {
        if (trackingNumberPatterns == null || trackingNumberPatterns.isEmpty()) {
            return false;
        }
        for (String pattern : trackingNumberPatterns) {
            if (Pattern.matches(pattern, trackingNumber)) {
                return true;
            }
        }
        return false;
    }
}
