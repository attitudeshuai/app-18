package com.babygearpass.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "escrow")
public class EscrowConfig {

    private int defaultPeriodHours = 72;

    private boolean autoCompleteAfterPeriod = true;
}
