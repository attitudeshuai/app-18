package com.babygearpass.service.impl;

import com.babygearpass.config.ExpressApiProperties;
import com.babygearpass.dto.logistics.ExpressTrackResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "express.api.provider", havingValue = "kuaidi100")
public class Kuaidi100ExpressQueryService extends AbstractExpressQueryService {

    public Kuaidi100ExpressQueryService(ExpressApiProperties properties, ObjectMapper objectMapper, RestTemplate restTemplate) {
        super(properties, objectMapper, restTemplate);
    }

    @Override
    public ExpressTrackResult queryTrack(String trackingNumber, String expressCompanyCode) {
        ExpressApiProperties.Kuaidi100Properties config = properties.getKuaidi100();
        if (!config.isEnabled() || config.getCustomer() == null || config.getKey() == null) {
            return ExpressTrackResult.error("CONFIG_ERROR", "快递100配置缺失，请配置customer和key");
        }

        try {
            String param = String.format("{\"com\":\"%s\",\"num\":\"%s\",\"phone\":\"\",\"from\":\"\",\"to\":\"\",\"resultv2\":\"1\"}",
                    expressCompanyCode != null ? expressCompanyCode.toLowerCase() : "", trackingNumber);
            String sign = md5(param + config.getKey() + config.getCustomer()).toLowerCase();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("customer", config.getCustomer());
            params.add("sign", sign);
            params.add("param", param);

            log.info("调用快递100查询接口, trackingNumber: {}, company: {}", trackingNumber, expressCompanyCode);
            String responseBody = doPost(config.getUrl(), params);
            log.debug("快递100响应: {}", responseBody);

            return parseResponse(responseBody, trackingNumber, expressCompanyCode);
        } catch (Exception e) {
            log.error("快递100查询失败, trackingNumber: {}, error: {}", trackingNumber, e.getMessage(), e);
            return buildErrorResult(trackingNumber, expressCompanyCode, e);
        }
    }

    private ExpressTrackResult parseResponse(String responseBody, String trackingNumber, String expressCompanyCode) throws Exception {
        ExpressTrackResult result = new ExpressTrackResult();
        result.setTrackingNumber(trackingNumber);
        result.setExpressCompanyCode(expressCompanyCode);
        result.setRawResponse(responseBody);

        JsonNode root = objectMapper.readTree(responseBody);
        String state = root.path("state").asText();
        String status = root.path("status").asText();

        if (!"200".equals(status)) {
            result.setSuccess(false);
            result.setErrorCode(status);
            result.setErrorMessage(root.path("message").asText("查询失败"));
            return result;
        }

        result.setSuccess(true);
        result.setStatus(mapState(state));
        result.setStatusText(mapStateText(state));

        JsonNode dataNode = root.path("data");
        List<ExpressTrackResult.TrackItem> tracks = new ArrayList<>();
        if (dataNode.isArray()) {
            for (int i = dataNode.size() - 1; i >= 0; i--) {
                JsonNode item = dataNode.get(i);
                ExpressTrackResult.TrackItem track = new ExpressTrackResult.TrackItem();
                track.setOccurredAt(parseDateTime(item.path("time").asText()));
                track.setDescription(item.path("context").asText());
                track.setLocation(item.path("location").asText());
                track.setStatusCode(mapState(item.path("status").asText()));
                track.setStatusName(mapStateText(item.path("status").asText()));
                tracks.add(track);
            }
        }
        result.setTracks(tracks);

        if (!tracks.isEmpty()) {
            ExpressTrackResult.TrackItem latest = tracks.get(tracks.size() - 1);
            result.setCurrentLocation(latest.getLocation());
            if (latest.getOccurredAt() != null) {
                result.setEstimatedDeliveryTime(latest.getOccurredAt().plusDays(2));
            }
            if ("3".equals(state) || "SIGNED".equals(result.getStatus())) {
                result.setActualDeliveryTime(latest.getOccurredAt());
            }
        }

        return result;
    }

    private String mapState(String state) {
        if (state == null) return "Unknown";
        switch (state) {
            case "0": return "InTransit";
            case "1": return "Accepted";
            case "2": return "Dispatching";
            case "3": return "Delivered";
            case "4": return "Returning";
            case "5": return "DeliveryFailed";
            case "6": return "Returned";
            case "7": return "Unknown";
            case "8": return "Arrived";
            case "10": return "DeliveryFailed";
            case "11": return "DeliveryFailed";
            case "12": return "DeliveryFailed";
            case "13": return "DeliveryFailed";
            case "14": return "InTransit";
            default: return "Unknown";
        }
    }

    private String mapStateText(String state) {
        if (state == null) return "未知";
        switch (state) {
            case "0": return "运输中";
            case "1": return "已揽收";
            case "2": return "派送中";
            case "3": return "已签收";
            case "4": return "退签中";
            case "5": return "投递失败";
            case "6": return "已退回";
            case "7": return "待查询";
            case "8": return "已到达";
            case "10": return "待签收";
            case "11": return "滞留";
            case "12": return "拒收";
            case "13": return "发错";
            case "14": return "正常";
            default: return "未知状态";
        }
    }

    @Override
    public String getProviderName() {
        return "快递100";
    }

    @Override
    public boolean isAvailable() {
        ExpressApiProperties.Kuaidi100Properties config = properties.getKuaidi100();
        return config.isEnabled() && config.getCustomer() != null && !config.getCustomer().isBlank()
                && config.getKey() != null && !config.getKey().isBlank();
    }
}
