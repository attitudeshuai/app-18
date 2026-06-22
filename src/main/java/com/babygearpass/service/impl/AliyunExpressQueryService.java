package com.babygearpass.service.impl;

import com.babygearpass.config.ExpressApiProperties;
import com.babygearpass.dto.logistics.ExpressTrackResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@ConditionalOnProperty(name = "express.api.provider", havingValue = "aliyun")
public class AliyunExpressQueryService extends AbstractExpressQueryService {

    public AliyunExpressQueryService(ExpressApiProperties properties, ObjectMapper objectMapper, RestTemplate restTemplate) {
        super(properties, objectMapper, restTemplate);
    }

    @Override
    public ExpressTrackResult queryTrack(String trackingNumber, String expressCompanyCode) {
        ExpressApiProperties.AliyunProperties config = properties.getAliyun();
        if (!config.isEnabled() || config.getAppCode() == null || config.getAppCode().isBlank()) {
            return ExpressTrackResult.error("CONFIG_ERROR", "阿里云市场配置缺失，请配置appCode");
        }

        try {
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("no", trackingNumber);
            if (expressCompanyCode != null && !expressCompanyCode.isBlank()) {
                queryParams.put("type", expressCompanyCode);
            }

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "APPCODE " + config.getAppCode());

            log.info("调用阿里云市场快递查询接口, trackingNumber: {}, company: {}", trackingNumber, expressCompanyCode);
            String responseBody = doGet(config.getUrl(), queryParams, headers);
            log.debug("阿里云市场响应: {}", responseBody);

            return parseResponse(responseBody, trackingNumber, expressCompanyCode);
        } catch (Exception e) {
            log.error("阿里云市场快递查询失败, trackingNumber: {}, error: {}", trackingNumber, e.getMessage(), e);
            return buildErrorResult(trackingNumber, expressCompanyCode, e);
        }
    }

    private ExpressTrackResult parseResponse(String responseBody, String trackingNumber, String expressCompanyCode) throws Exception {
        ExpressTrackResult result = new ExpressTrackResult();
        result.setTrackingNumber(trackingNumber);
        result.setExpressCompanyCode(expressCompanyCode);
        result.setRawResponse(responseBody);

        JsonNode root = objectMapper.readTree(responseBody);
        String statusCode = root.path("status").asText();
        String msg = root.path("msg").asText();

        if (!"0".equals(statusCode) && !"200".equals(statusCode)) {
            result.setSuccess(false);
            result.setErrorCode(statusCode);
            result.setErrorMessage(msg);
            return result;
        }

        JsonNode resultNode = root.path("result");
        if (resultNode.isMissingNode() || resultNode.isNull()) {
            result.setSuccess(false);
            result.setErrorCode("NO_DATA");
            result.setErrorMessage("暂无物流轨迹");
            return result;
        }

        result.setSuccess(true);
        result.setExpressCompanyCode(resultNode.path("type").asText(expressCompanyCode));
        result.setExpressCompanyName(resultNode.path("expName").asText(""));

        String number = resultNode.path("number").asText("");
        result.setTrackingNumber(number != null && !number.isBlank() ? number : trackingNumber);

        String delvStatus = resultNode.path("delvstatus").asText();
        result.setStatus(mapDelvStatus(delvStatus));
        result.setStatusText(mapDelvStatusText(delvStatus));

        JsonNode listNode = resultNode.path("list");
        List<ExpressTrackResult.TrackItem> tracks = new ArrayList<>();
        if (listNode.isArray()) {
            for (JsonNode item : listNode) {
                ExpressTrackResult.TrackItem track = new ExpressTrackResult.TrackItem();
                track.setOccurredAt(parseDateTime(item.path("time").asText()));
                track.setStatusName(item.path("status").asText(""));
                track.setLocation(item.path("location").asText(""));
                track.setDescription(item.path("status").asText(""));
                tracks.add(track);
            }
        }
        tracks.sort(Comparator.comparing(
                t -> t.getOccurredAt() != null ? t.getOccurredAt() : java.time.LocalDateTime.MIN));
        result.setTracks(tracks);

        if (!tracks.isEmpty()) {
            ExpressTrackResult.TrackItem latest = tracks.get(tracks.size() - 1);
            result.setCurrentLocation(latest.getLocation() != null && !latest.getLocation().isBlank()
                    ? latest.getLocation() : latest.getStatusName());
            if (latest.getOccurredAt() != null) {
                result.setEstimatedDeliveryTime(latest.getOccurredAt().plusDays(2));
            }
            if ("SIGNED".equals(result.getStatus()) || "3".equals(delvStatus)) {
                result.setActualDeliveryTime(latest.getOccurredAt());
            }
        }

        return result;
    }

    private String mapDelvStatus(String delvStatus) {
        if (delvStatus == null) return "Unknown";
        switch (delvStatus) {
            case "0": return "InTransit";
            case "1": return "Accepted";
            case "2": return "Dispatching";
            case "3": return "Delivered";
            case "4": return "DeliveryFailed";
            case "5": return "Returning";
            case "6": return "Returned";
            case "10": return "Arrived";
            default: return "Unknown";
        }
    }

    private String mapDelvStatusText(String delvStatus) {
        if (delvStatus == null) return "未知";
        switch (delvStatus) {
            case "0": return "运输中";
            case "1": return "已揽收";
            case "2": return "派送中";
            case "3": return "已签收";
            case "4": return "派送失败";
            case "5": return "退签中";
            case "6": return "已退回";
            case "10": return "已到达";
            default: return "未知状态";
        }
    }

    @Override
    public String getProviderName() {
        return "阿里云市场";
    }

    @Override
    public boolean isAvailable() {
        ExpressApiProperties.AliyunProperties config = properties.getAliyun();
        return config.isEnabled() && config.getAppCode() != null && !config.getAppCode().isBlank();
    }
}
