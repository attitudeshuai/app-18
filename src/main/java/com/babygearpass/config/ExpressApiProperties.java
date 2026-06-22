package com.babygearpass.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "express.api")
public class ExpressApiProperties {

    private String provider = "mock";

    private Kuaidi100Properties kuaidi100 = new Kuaidi100Properties();

    private KuaidiniaoProperties kuaidiniao = new KuaidiniaoProperties();

    private AliyunProperties aliyun = new AliyunProperties();

    private Integer connectTimeoutMs = 5000;

    private Integer readTimeoutMs = 10000;

    private Integer retryCount = 2;

    @Data
    public static class Kuaidi100Properties {
        private String customer;
        private String key;
        private String url = "https://poll.kuaidi100.com/poll/query.do";
        private String autoDetectUrl = "https://www.kuaidi100.com/autonumber/auto";
        private boolean enabled = false;
    }

    @Data
    public static class KuaidiniaoProperties {
        private String eBusinessId;
        private String appKey;
        private String url = "https://api.kdniao.com/Ebusiness/EbusinessOrderHandle.aspx";
        private boolean sandbox = false;
        private boolean enabled = false;
    }

    @Data
    public static class AliyunProperties {
        private String appCode;
        private String url = "https://wuliu.market.alicloudapi.com/kdi";
        private boolean enabled = false;
    }
}
