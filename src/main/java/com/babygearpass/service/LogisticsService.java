package com.babygearpass.service;

import com.babygearpass.dto.logistics.*;
import com.babygearpass.dto.common.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LogisticsService {

    LogisticsDTO createLogistics(String username, LogisticsRequest request);

    LogisticsDTO updateLogistics(Long id, String username, LogisticsRequest request);

    LogisticsDTO getLogisticsById(Long id);

    LogisticsDTO getLogisticsByHandoverId(Long handoverId);

    LogisticsDTO getLogisticsByTrackingNumber(String trackingNumber);

    PageResponse<LogisticsDTO> getAllLogistics(String status, String syncStatus, Pageable pageable);

    PageResponse<LogisticsDTO> getMyLogistics(String username, Pageable pageable);

    void deleteLogistics(Long id, String username);

    LogisticsDTO syncLogistics(Long id, String username);

    LogisticsDTO syncLogisticsByHandoverId(Long handoverId, String username);

    List<ExpressCompanyDTO> getSupportedExpressCompanies();

    ExpressCompanyDTO identifyExpressCompany(String trackingNumber);

    PageResponse<LogisticsSyncLogDTO> getSyncLogs(Long logisticsId, Boolean onlyFailed, Pageable pageable);

    LogisticsDTO updateLogisticsManually(Long id, String username, LogisticsManualUpdateRequest request);
}
