package com.babygearpass.service.impl;

import com.babygearpass.config.ExpressApiProperties;
import com.babygearpass.dto.logistics.ExpressTrackResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@ConditionalOnProperty(name = "express.api.provider", havingValue = "kuaidiniao")
public class KuaidiniaoExpressQueryService extends AbstractExpressQueryService {

    private static final String REQUEST_TYPE_TRACE = "1002";

    public KuaidiniaoExpressQueryService(ExpressApiProperties properties, ObjectMapper objectMapper, RestTemplate restTemplate) {
        super(properties, objectMapper, restTemplate);
    }

    @Override
    public ExpressTrackResult queryTrack(String trackingNumber, String expressCompanyCode) {
        ExpressApiProperties.KuaidiniaoProperties config = properties.getKuaidiniao();
        if (!config.isEnabled() || config.getEBusinessId() == null || config.getAppKey() == null) {
            return ExpressTrackResult.error("CONFIG_ERROR", "快递鸟配置缺失，请配置eBusinessId和appKey");
        }

        try {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("OrderCode", "");
            requestData.put("ShipperCode", expressCompanyCode != null ? expressCompanyCode.toUpperCase() : "");
            requestData.put("LogisticCode", trackingNumber);
            if (config.isSandbox()) {
                requestData.put("CustomerName", "");
            }

            String requestDataJson = objectMapper.writeValueAsString(requestData);
            String dataSign = encrypt(requestDataJson, config.getAppKey());

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("RequestData", requestDataJson);
            params.add("EBusinessID", config.getEBusinessId());
            params.add("RequestType", REQUEST_TYPE_TRACE);
            params.add("DataSign", dataSign);
            params.add("DataType", "2");

            String url = config.isSandbox()
                    ? "https://sandboxapi.kdniao.com:8080/kdniaosandbox/gateway/exterfaceInvoke.json"
                    : config.getUrl();

            log.info("调用快递鸟查询接口, trackingNumber: {}, company: {}, sandbox: {}",
                    trackingNumber, expressCompanyCode, config.isSandbox());
            String responseBody = doPost(url, params);
            log.debug("快递鸟响应: {}", responseBody);

            return parseResponse(responseBody, trackingNumber, expressCompanyCode);
        } catch (Exception e) {
            log.error("快递鸟查询失败, trackingNumber: {}, error: {}", trackingNumber, e.getMessage(), e);
            return buildErrorResult(trackingNumber, expressCompanyCode, e);
        }
    }

    private ExpressTrackResult parseResponse(String responseBody, String trackingNumber, String expressCompanyCode) throws Exception {
        ExpressTrackResult result = new ExpressTrackResult();
        result.setTrackingNumber(trackingNumber);
        result.setExpressCompanyCode(expressCompanyCode);
        result.setRawResponse(responseBody);

        JsonNode root = objectMapper.readTree(responseBody);
        boolean success = root.path("Success").asBoolean(false);
        String code = root.path("Code").asText();

        if (!success || !"100".equals(code) && !"200".equals(code)) {
            result.setSuccess(false);
            result.setErrorCode(code);
            result.setErrorMessage(root.path("Reason").asText("查询失败"));
            return result;
        }

        result.setSuccess(true);
        String state = root.path("State").asText();
        result.setStatus(mapState(state));
        result.setStatusText(mapStateText(state));
        result.setExpressCompanyCode(root.path("ShipperCode").asText(expressCompanyCode));
        result.setExpressCompanyName(root.path("ShipperName").asText(""));

        JsonNode traces = root.path("Traces");
        List<ExpressTrackResult.TrackItem> trackItems = new ArrayList<>();
        if (traces.isArray()) {
            for (JsonNode trace : traces) {
                ExpressTrackResult.TrackItem item = new ExpressTrackResult.TrackItem();
                item.setOccurredAt(parseDateTime(trace.path("AcceptTime").asText()));
                item.setDescription(trace.path("AcceptStation").asText());
                item.setLocation(trace.path("Location").asText(""));
                String action = trace.path("Action").asText("");
                item.setStatusCode(mapAction(action));
                item.setStatusName(mapActionText(action));
                item.setOperatorName(trace.path("Operator").asText(""));
                item.setOperatorPhone(trace.path("OperatorPhone").asText(""));
                trackItems.add(item);
            }
        }
        trackItems.sort(Comparator.comparing(
                t -> t.getOccurredAt() != null ? t.getOccurredAt() : java.time.LocalDateTime.MIN));
        result.setTracks(trackItems);

        if (!trackItems.isEmpty()) {
            ExpressTrackResult.TrackItem latest = trackItems.get(trackItems.size() - 1);
            result.setCurrentLocation(latest.getLocation() != null && !latest.getLocation().isBlank()
                    ? latest.getLocation() : extractLocation(latest.getDescription()));
            if (latest.getOccurredAt() != null) {
                result.setEstimatedDeliveryTime(latest.getOccurredAt().plusDays(2));
            }
            if ("3".equals(state) || "SIGNED".equals(result.getStatus())) {
                result.setActualDeliveryTime(latest.getOccurredAt());
            }
        }

        return result;
    }

    private String extractLocation(String description) {
        if (description == null || description.isBlank()) return "";
        int idx = description.indexOf("【");
        int endIdx = description.indexOf("】");
        if (idx >= 0 && endIdx > idx) {
            return description.substring(idx + 1, endIdx);
        }
        return "";
    }

    private String mapState(String state) {
        if (state == null) return "Unknown";
        switch (state) {
            case "0": return "NoRecord";
            case "1": return "Accepted";
            case "2": return "InTransit";
            case "3": return "Delivered";
            case "4": return "DeliveryFailed";
            case "5": return "Returning";
            case "6": return "Returned";
            case "7": return "Dispatching";
            case "8": return "Arrived";
            case "9": return "CustomsClearance";
            case "10": return "DeliveryFailed";
            case "11": return "Unknown";
            case "12": return "InTransit";
            case "13": return "Dispatching";
            case "14": return "InTransit";
            case "15": return "InTransit";
            case "16": return "Unknown";
            case "17": return "Unknown";
            default: return "Unknown";
        }
    }

    private String mapStateText(String state) {
        if (state == null) return "未知";
        switch (state) {
            case "0": return "无轨迹";
            case "1": return "已揽收";
            case "2": return "运输中";
            case "3": return "已签收";
            case "4": return "派送失败";
            case "5": return "退签中";
            case "6": return "已退回";
            case "7": return "派送中";
            case "8": return "已到达";
            case "9": return "清关中";
            case "10": return "派送失败";
            case "11": return "已拒签收";
            case "12": return "滞留";
            case "13": return "二次派送";
            case "14": return "中转中";
            case "15": return "调拨中";
            case "16": return "代签收";
            case "17": return "异常签收";
            default: return "未知状态";
        }
    }

    private String mapAction(String action) {
        if (action == null || action.isBlank()) return "Unknown";
        switch (action) {
            case "1": return "Accepted";
            case "2": return "InTransit";
            case "3": return "Dispatching";
            case "4": return "Delivered";
            case "5": return "DeliveryFailed";
            case "6": return "Returning";
            case "7": return "Unknown";
            case "8": return "CustomsClearance";
            case "9": return "Unknown";
            case "10": return "InTransit";
            case "11": return "InTransit";
            case "12": return "InTransit";
            case "13": return "Unknown";
            case "14": return "Dispatching";
            default: return "Unknown";
        }
    }

    private String mapActionText(String action) {
        if (action == null || action.isBlank()) return "未知";
        switch (action) {
            case "1": return "揽收";
            case "2": return "在途中";
            case "3": return "派件";
            case "4": return "签收";
            case "5": return "派送失败";
            case "6": return "退签";
            case "7": return "投柜";
            case "8": return "清关";
            case "9": return "退柜";
            case "10": return "转单";
            case "11": return "退单";
            case "12": return "转寄";
            case "13": return "改址";
            case "14": return "再派";
            default: return "未知操作";
        }
    }

    private String encrypt(String content, String keyValue) {
        try {
            String md5Str = md5(content + keyValue);
            return Base64.getEncoder().encodeToString(md5Str.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("快递鸟签名加密失败", e);
        }
    }

    @Override
    public String getProviderName() {
        return "快递鸟";
    }

    @Override
    public boolean isAvailable() {
        ExpressApiProperties.KuaidiniaoProperties config = properties.getKuaidiniao();
        return config.isEnabled() && config.getEBusinessId() != null && !config.getEBusinessId().isBlank()
                && config.getAppKey() != null && !config.getAppKey().isBlank();
    }
}
