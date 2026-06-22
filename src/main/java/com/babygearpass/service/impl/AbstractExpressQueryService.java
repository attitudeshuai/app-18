package com.babygearpass.service.impl;

import com.babygearpass.config.ExpressApiProperties;
import com.babygearpass.dto.logistics.ExpressTrackResult;
import com.babygearpass.service.ExpressQueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public abstract class AbstractExpressQueryService implements ExpressQueryService {

    protected static final Logger log = LoggerFactory.getLogger(AbstractExpressQueryService.class);

    protected final ExpressApiProperties properties;
    protected final ObjectMapper objectMapper;
    protected final RestTemplate restTemplate;

    protected static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    protected AbstractExpressQueryService(ExpressApiProperties properties, ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    protected String doPost(String url, MultiValueMap<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("HTTP POST请求失败, url: {}, params: {}, error: {}", url, params, e.getMessage(), e);
            throw new RuntimeException("快递查询接口调用失败: " + e.getMessage(), e);
        }
    }

    protected String doPostJson(String url, Object body, Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }
        try {
            String jsonBody = body instanceof String ? (String) body : objectMapper.writeValueAsString(body);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, httpHeaders);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("HTTP POST JSON请求失败, url: {}, error: {}", url, e.getMessage(), e);
            throw new RuntimeException("快递查询接口调用失败: " + e.getMessage(), e);
        }
    }

    protected String doGet(String url, Map<String, String> queryParams, Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
            if (queryParams != null) {
                queryParams.forEach(builder::queryParam);
            }
            URI uri = builder.build().toUri();
            HttpEntity<Void> entity = new HttpEntity<>(httpHeaders);
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("HTTP GET请求失败, url: {}, error: {}", url, e.getMessage(), e);
            throw new RuntimeException("快递查询接口调用失败: " + e.getMessage(), e);
        }
    }

    protected String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    protected LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        try {
            String cleaned = dateTimeStr.trim().replaceAll("[年月]", "-").replaceAll("[日]", " ").replace("T", " ");
            if (cleaned.contains(".")) {
                cleaned = cleaned.substring(0, cleaned.indexOf("."));
            }
            if (cleaned.length() == 10) {
                cleaned = cleaned + " 00:00:00";
            } else if (cleaned.length() == 16) {
                cleaned = cleaned + ":00";
            }
            return LocalDateTime.parse(cleaned, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            log.warn("日期时间解析失败: {} -> {}", dateTimeStr, e.getMessage());
            return LocalDateTime.now();
        }
    }

    protected LocalDateTime parseTimestamp(Long timestamp) {
        if (timestamp == null) return null;
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    protected ExpressTrackResult buildErrorResult(String trackingNumber, String expressCompanyCode, Exception e) {
        ExpressTrackResult result = ExpressTrackResult.error("API_ERROR", e.getMessage());
        result.setTrackingNumber(trackingNumber);
        result.setExpressCompanyCode(expressCompanyCode);
        return result;
    }
}
