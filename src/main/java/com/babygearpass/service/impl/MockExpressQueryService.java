package com.babygearpass.service.impl;

import com.babygearpass.config.ExpressApiProperties;
import com.babygearpass.dto.logistics.ExpressTrackResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "express.api.provider", havingValue = "mock", matchIfMissing = true)
public class MockExpressQueryService implements com.babygearpass.service.ExpressQueryService {

    private static final Logger log = LoggerFactory.getLogger(MockExpressQueryService.class);

    @Override
    public ExpressTrackResult queryTrack(String trackingNumber, String expressCompanyCode) {
        log.warn("【警告】当前使用的是 Mock 快递查询服务，返回的是模拟数据！" +
                "请在 application.yml 中配置 express.api.provider=kuaidi100/kuaidiniao/aliyun 并填入对应密钥，以对接真实快递查询接口。");

        ExpressTrackResult result = new ExpressTrackResult();
        result.setSuccess(true);
        result.setTrackingNumber(trackingNumber);
        result.setExpressCompanyCode(expressCompanyCode);
        result.setExpressCompanyName(resolveCompanyName(expressCompanyCode));
        result.setStatus("InTransit");
        result.setStatusText("运输中");

        List<ExpressTrackResult.TrackItem> tracks = new ArrayList<>();
        LocalDateTime base = LocalDateTime.now().minusDays(2);

        String[][] statuses = {
                {"ACCEPTED", "已揽收", "快件已被【" + resolveCompanyName(expressCompanyCode) + "】揽收",
                        "上海市浦东新区转运中心"},
                {"TRANSIT", "运输中", "快件已到达【上海转运中心】，正在分拣中",
                        "上海市转运中心"},
                {"TRANSIT", "运输中", "快件已从【上海转运中心】发出，下一站【北京转运中心】",
                        "北京途中"},
                {"ARRIVED", "已到达", "快件已到达【北京转运中心】",
                        "北京市转运中心"},
                {"DISPATCHING", "派送中", "快件已到达【北京市朝阳区营业点】，快递员正在派送中",
                        "北京市朝阳区"},
        };

        for (int i = 0; i < statuses.length; i++) {
            ExpressTrackResult.TrackItem item = new ExpressTrackResult.TrackItem();
            item.setStatusCode(statuses[i][0]);
            item.setStatusName(statuses[i][1]);
            item.setDescription(statuses[i][2]);
            item.setLocation(statuses[i][3]);
            item.setOccurredAt(base.plusHours(i * 6));
            tracks.add(item);
        }

        result.setTracks(tracks);
        if (!tracks.isEmpty()) {
            ExpressTrackResult.TrackItem latest = tracks.get(tracks.size() - 1);
            result.setCurrentLocation(latest.getLocation());
            result.setEstimatedDeliveryTime(latest.getOccurredAt().plusHours(8));
        }
        result.setRawResponse("{\"mock\":true,\"message\":\"当前为模拟数据，请配置真实快递API\"}");

        return result;
    }

    private String resolveCompanyName(String code) {
        if (code == null) return "快递公司";
        switch (code.toUpperCase()) {
            case "SF": return "顺丰速运";
            case "YTO": return "圆通速递";
            case "ZTO": return "中通快递";
            case "STO": return "申通快递";
            case "YD": return "韵达速递";
            case "JD": return "京东物流";
            case "EMS": return "EMS邮政快递";
            case "DBL": return "德邦快递";
            case "HTKY": return "百世快递";
            default: return "其他快递";
        }
    }

    @Override
    public String getProviderName() {
        return "Mock模拟(未对接真实API)";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
