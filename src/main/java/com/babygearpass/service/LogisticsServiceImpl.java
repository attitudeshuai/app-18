package com.babygearpass.service;

import com.babygearpass.dto.common.PageResponse;
import com.babygearpass.dto.logistics.*;
import com.babygearpass.entity.*;
import com.babygearpass.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogisticsServiceImpl implements LogisticsService {

    private static final Logger logger = LoggerFactory.getLogger(LogisticsServiceImpl.class);

    private final LogisticsRepository logisticsRepository;
    private final LogisticsStatusLogRepository statusLogRepository;
    private final LogisticsSyncLogRepository syncLogRepository;
    private final GearHandoverRepository handoverRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final List<ExpressCompanyDTO> expressCompanies;

    @Override
    @Transactional
    public LogisticsDTO createLogistics(String username, LogisticsRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        GearHandover handover = handoverRepository.findById(request.getHandoverId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "交接记录不存在"));

        if (!handover.getGiver().getUsername().equals(username) && !handover.getReceiver().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作此交接记录");
        }

        if (logisticsRepository.findByHandoverId(request.getHandoverId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该交接单已存在物流信息");
        }

        ExpressCompanyDTO companyDTO = request.getExpressCompanyCode() != null
                ? findExpressCompanyByCode(request.getExpressCompanyCode())
                : identifyExpressCompany(request.getTrackingNumber());

        if (companyDTO == null) {
            companyDTO = findExpressCompanyByCode("OTHER");
        }

        Logistics logistics = new Logistics();
        logistics.setHandover(handover);
        logistics.setTrackingNumber(request.getTrackingNumber());
        logistics.setExpressCompanyCode(companyDTO.getCode());
        logistics.setExpressCompanyName(companyDTO.getName());
        logistics.setCurrentStatus("Pending");
        logistics.setSyncStatus("Pending");
        logistics.setSenderName(request.getSenderName());
        logistics.setSenderPhone(request.getSenderPhone());
        logistics.setSenderAddress(request.getSenderAddress());
        logistics.setReceiverName(request.getReceiverName());
        logistics.setReceiverPhone(request.getReceiverPhone());
        logistics.setReceiverAddress(request.getReceiverAddress());
        logistics.setRemark(request.getRemark());

        logisticsRepository.save(logistics);

        doSyncLogistics(logistics, user, "Manual");

        return toDTO(logistics);
    }

    @Override
    @Transactional
    public LogisticsDTO updateLogistics(Long id, String username, LogisticsRequest request) {
        Logistics logistics = getLogisticsEntity(id);
        validatePermission(logistics, username);

        if (request.getTrackingNumber() != null && !request.getTrackingNumber().equals(logistics.getTrackingNumber())) {
            logistics.setTrackingNumber(request.getTrackingNumber());
            ExpressCompanyDTO companyDTO = identifyExpressCompany(request.getTrackingNumber());
            if (companyDTO != null) {
                logistics.setExpressCompanyCode(companyDTO.getCode());
                logistics.setExpressCompanyName(companyDTO.getName());
            }
        }

        if (request.getExpressCompanyCode() != null) {
            logistics.setExpressCompanyCode(request.getExpressCompanyCode());
            if (request.getExpressCompanyName() != null) {
                logistics.setExpressCompanyName(request.getExpressCompanyName());
            } else {
                ExpressCompanyDTO companyDTO = findExpressCompanyByCode(request.getExpressCompanyCode());
                if (companyDTO != null) {
                    logistics.setExpressCompanyName(companyDTO.getName());
                }
            }
        }

        if (request.getSenderName() != null) logistics.setSenderName(request.getSenderName());
        if (request.getSenderPhone() != null) logistics.setSenderPhone(request.getSenderPhone());
        if (request.getSenderAddress() != null) logistics.setSenderAddress(request.getSenderAddress());
        if (request.getReceiverName() != null) logistics.setReceiverName(request.getReceiverName());
        if (request.getReceiverPhone() != null) logistics.setReceiverPhone(request.getReceiverPhone());
        if (request.getReceiverAddress() != null) logistics.setReceiverAddress(request.getReceiverAddress());
        if (request.getRemark() != null) logistics.setRemark(request.getRemark());

        logisticsRepository.save(logistics);

        return toDTO(logistics);
    }

    @Override
    public LogisticsDTO getLogisticsById(Long id) {
        Logistics logistics = getLogisticsEntity(id);
        return toDTO(logistics);
    }

    @Override
    public LogisticsDTO getLogisticsByHandoverId(Long handoverId) {
        Logistics logistics = logisticsRepository.findByHandoverId(handoverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "该交接单暂无物流信息"));
        return toDTO(logistics);
    }

    @Override
    public LogisticsDTO getLogisticsByTrackingNumber(String trackingNumber) {
        Logistics logistics = logisticsRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "未找到该物流单号的信息"));
        return toDTO(logistics);
    }

    @Override
    public PageResponse<LogisticsDTO> getAllLogistics(String status, String syncStatus, Pageable pageable) {
        Page<Logistics> logisticsPage;
        if (status != null && !status.isBlank()) {
            logisticsPage = logisticsRepository.findByCurrentStatus(status, pageable);
        } else if (syncStatus != null && !syncStatus.isBlank()) {
            logisticsPage = logisticsRepository.findBySyncStatus(syncStatus, pageable);
        } else {
            logisticsPage = logisticsRepository.findAll(pageable);
        }
        return toPageResponse(logisticsPage);
    }

    @Override
    public PageResponse<LogisticsDTO> getMyLogistics(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        Page<Logistics> logisticsPage = logisticsRepository.findByUserId(user.getId(), pageable);
        return toPageResponse(logisticsPage);
    }

    @Override
    @Transactional
    public void deleteLogistics(Long id, String username) {
        Logistics logistics = getLogisticsEntity(id);
        validatePermission(logistics, username);
        logisticsRepository.delete(logistics);
    }

    @Override
    @Transactional
    public LogisticsDTO syncLogistics(Long id, String username) {
        Logistics logistics = getLogisticsEntity(id);
        validatePermission(logistics, username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        doSyncLogistics(logistics, user, "Manual");
        return toDTO(logistics);
    }

    @Override
    @Transactional
    public LogisticsDTO syncLogisticsByHandoverId(Long handoverId, String username) {
        Logistics logistics = logisticsRepository.findByHandoverId(handoverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "该交接单暂无物流信息"));
        validatePermission(logistics, username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        doSyncLogistics(logistics, user, "Manual");
        return toDTO(logistics);
    }

    @Override
    public List<ExpressCompanyDTO> getSupportedExpressCompanies() {
        return expressCompanies;
    }

    @Override
    public ExpressCompanyDTO identifyExpressCompany(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.isBlank()) {
            return null;
        }
        String normalizedTrackingNumber = trackingNumber.trim().toUpperCase();
        for (ExpressCompanyDTO company : expressCompanies) {
            if (company.matchesTrackingNumber(normalizedTrackingNumber)) {
                return company;
            }
        }
        return findExpressCompanyByCode("OTHER");
    }

    @Override
    public PageResponse<LogisticsSyncLogDTO> getSyncLogs(Long logisticsId, Boolean onlyFailed, Pageable pageable) {
        Page<LogisticsSyncLog> logPage;
        if (Boolean.TRUE.equals(onlyFailed)) {
            logPage = syncLogRepository.findByIsSuccessFalse(pageable);
        } else if (logisticsId != null) {
            logPage = syncLogRepository.findByLogisticsId(logisticsId, pageable);
        } else {
            logPage = syncLogRepository.findAll(pageable);
        }
        List<LogisticsSyncLogDTO> dtoList = logPage.getContent().stream()
                .map(this::toSyncLogDTO)
                .collect(Collectors.toList());
        return new PageResponse<>(dtoList, logPage.getNumber(), logPage.getSize(),
                logPage.getTotalElements(), logPage.getTotalPages());
    }

    @Override
    @Transactional
    public LogisticsDTO updateLogisticsManually(Long id, String username, LogisticsManualUpdateRequest request) {
        Logistics logistics = getLogisticsEntity(id);
        validatePermission(logistics, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        LogisticsSyncLog syncLog = new LogisticsSyncLog();
        syncLog.setLogistics(logistics);
        syncLog.setTrackingNumber(logistics.getTrackingNumber());
        syncLog.setExpressCompanyCode(logistics.getExpressCompanyCode());
        syncLog.setSyncType("Manual");
        syncLog.setOperatorId(user.getId());
        syncLog.setSyncStartTime(LocalDateTime.now());
        syncLog.setIsSuccess(true);
        syncLog.setRequestDetail("Manual update by user: " + username);

        if (request.getCurrentStatus() != null) {
            logistics.setCurrentStatus(request.getCurrentStatus());
            if ("Delivered".equals(request.getCurrentStatus()) || "Signed".equals(request.getCurrentStatus())) {
                logistics.setActualDeliveryTime(LocalDateTime.now());
                handleDeliveryCompleted(logistics);
            }
        }
        if (request.getCurrentLocation() != null) {
            logistics.setCurrentLocation(request.getCurrentLocation());
        }
        if (request.getEstimatedDeliveryTime() != null) {
            logistics.setEstimatedDeliveryTime(request.getEstimatedDeliveryTime());
        }
        if (request.getRemark() != null) {
            logistics.setRemark(request.getRemark());
        }

        if (request.getNewStatusCode() != null && request.getNewStatusName() != null) {
            LogisticsStatusLog statusLog = new LogisticsStatusLog();
            statusLog.setLogistics(logistics);
            statusLog.setStatusCode(request.getNewStatusCode());
            statusLog.setStatusName(request.getNewStatusName());
            statusLog.setLocation(request.getNewLocation() != null ? request.getNewLocation() : (logistics.getCurrentLocation() != null ? logistics.getCurrentLocation() : "未知"));
            statusLog.setDescription(request.getNewDescription());
            statusLog.setOccurredAt(request.getNewOccurredAt() != null ? request.getNewOccurredAt() : LocalDateTime.now());
            statusLogRepository.save(statusLog);

            if (logistics.getCurrentLocation() == null && request.getNewLocation() != null) {
                logistics.setCurrentLocation(request.getNewLocation());
            }
        }

        logistics.setLastSyncTime(LocalDateTime.now());
        logistics.setSyncStatus("Success");
        logistics.setSyncFailCount(0);

        syncLog.setSyncEndTime(LocalDateTime.now());
        syncLogRepository.save(syncLog);

        logisticsRepository.save(logistics);

        return toDTO(logistics);
    }

    private void doSyncLogistics(Logistics logistics, User operator, String syncType) {
        LogisticsSyncLog syncLog = new LogisticsSyncLog();
        syncLog.setLogistics(logistics);
        syncLog.setTrackingNumber(logistics.getTrackingNumber());
        syncLog.setExpressCompanyCode(logistics.getExpressCompanyCode());
        syncLog.setSyncType(syncType);
        syncLog.setOperatorId(operator != null ? operator.getId() : null);
        syncLog.setSyncStartTime(LocalDateTime.now());

        try {
            List<LogisticsStatusLog> fetchedStatusLogs = fetchLogisticsStatusFromApi(
                    logistics.getTrackingNumber(),
                    logistics.getExpressCompanyCode()
            );

            statusLogRepository.deleteByLogisticsId(logistics.getId());

            for (LogisticsStatusLog statusLog : fetchedStatusLogs) {
                statusLog.setLogistics(logistics);
                statusLogRepository.save(statusLog);
            }

            if (!fetchedStatusLogs.isEmpty()) {
                LogisticsStatusLog latestLog = fetchedStatusLogs.get(fetchedStatusLogs.size() - 1);
                logistics.setCurrentStatus(mapApiStatusToInternal(latestLog.getStatusCode()));
                logistics.setCurrentLocation(latestLog.getLocation());
            }

            logistics.setEstimatedDeliveryTime(estimateDeliveryTime(fetchedStatusLogs, logistics));

            String finalStatus = logistics.getCurrentStatus();
            if ("Delivered".equals(finalStatus) || "Signed".equals(finalStatus)) {
                if (logistics.getActualDeliveryTime() == null) {
                    logistics.setActualDeliveryTime(LocalDateTime.now());
                }
                handleDeliveryCompleted(logistics);
            }

            logistics.setLastSyncTime(LocalDateTime.now());
            logistics.setSyncStatus("Success");
            logistics.setSyncFailCount(0);

            syncLog.setIsSuccess(true);
            syncLog.setResponseDetail("Fetched " + fetchedStatusLogs.size() + " status logs");

        } catch (Exception e) {
            logger.error("物流同步失败, trackingNumber: {}, company: {}, error: {}",
                    logistics.getTrackingNumber(), logistics.getExpressCompanyCode(), e.getMessage(), e);

            logistics.setSyncStatus("Failed");
            logistics.setSyncFailCount(logistics.getSyncFailCount() + 1);

            syncLog.setIsSuccess(false);
            syncLog.setErrorCode("SYNC_ERROR");
            syncLog.setErrorMessage(e.getMessage());
            syncLog.setResponseDetail(e.toString());
        }

        syncLog.setSyncEndTime(LocalDateTime.now());
        syncLogRepository.save(syncLog);
        logisticsRepository.save(logistics);
    }

    private List<LogisticsStatusLog> fetchLogisticsStatusFromApi(String trackingNumber, String companyCode) {
        List<LogisticsStatusLog> mockLogs = new ArrayList<>();

        String[][] statuses = {
                {"ACCEPTED", "已揽收", "快件已被揽收"},
                {"TRANSIT", "运输中", "快件正在运输途中"},
                {"DISPATCHING", "派送中", "快件正在派送中"},
                {"DELIVERED", "已签收", "快件已被签收"}
        };

        LocalDateTime baseTime = LocalDateTime.now().minusDays(2);
        String[] locations = {"上海市浦东新区", "上海市转运中心", "北京市转运中心", "北京市朝阳区"};

        for (int i = 0; i < statuses.length; i++) {
            LogisticsStatusLog log = new LogisticsStatusLog();
            log.setStatusCode(statuses[i][0]);
            log.setStatusName(statuses[i][1]);
            log.setLocation(locations[Math.min(i, locations.length - 1)]);
            log.setDescription(statuses[i][2]);
            log.setOccurredAt(baseTime.plusHours(i * 6));
            mockLogs.add(log);
        }

        return mockLogs;
    }

    private String mapApiStatusToInternal(String apiStatus) {
        if (apiStatus == null) return "Unknown";
        switch (apiStatus.toUpperCase()) {
            case "ACCEPTED":
            case "COLLECTED":
            case "PICKED_UP":
                return "Accepted";
            case "TRANSIT":
            case "IN_TRANSIT":
            case "TRANSPORTING":
                return "InTransit";
            case "ARRIVED":
            case "ARRIVED_AT_DESTINATION":
                return "Arrived";
            case "DISPATCHING":
            case "OUT_FOR_DELIVERY":
                return "Dispatching";
            case "DELIVERED":
            case "SIGNED":
            case "SIGN_IN":
                return "Delivered";
            case "FAILED":
            case "DELIVERY_FAILED":
                return "DeliveryFailed";
            case "RETURNING":
                return "Returning";
            case "RETURNED":
                return "Returned";
            default:
                return "Unknown";
        }
    }

    private LocalDateTime estimateDeliveryTime(List<LogisticsStatusLog> statusLogs, Logistics logistics) {
        if (logistics.getEstimatedDeliveryTime() != null) {
            return logistics.getEstimatedDeliveryTime();
        }
        if (statusLogs == null || statusLogs.isEmpty()) {
            return LocalDateTime.now().plusDays(3);
        }
        LogisticsStatusLog latest = statusLogs.get(statusLogs.size() - 1);
        String latestStatus = latest.getStatusCode().toUpperCase();
        switch (latestStatus) {
            case "ACCEPTED":
            case "COLLECTED":
            case "PICKED_UP":
                return latest.getOccurredAt().plusDays(3);
            case "TRANSIT":
            case "IN_TRANSIT":
            case "TRANSPORTING":
                return latest.getOccurredAt().plusDays(2);
            case "ARRIVED":
            case "ARRIVED_AT_DESTINATION":
                return latest.getOccurredAt().plusDays(1);
            case "DISPATCHING":
            case "OUT_FOR_DELIVERY":
                return latest.getOccurredAt().plusHours(8);
            default:
                return latest.getOccurredAt().plusDays(2);
        }
    }

    private void handleDeliveryCompleted(Logistics logistics) {
        GearHandover handover = logistics.getHandover();
        if (handover == null) return;

        if (!"Completed".equals(handover.getStatus())) {
            handover.setStatus("Completed");
            handover.setConfirmedByReceiver(true);
            handover.setConfirmedAt(LocalDateTime.now());
            handoverRepository.save(handover);

            createNotification(
                    handover.getReceiver(),
                    "Logistics_Delivered",
                    "包裹已签收",
                    "您的包裹 " + logistics.getTrackingNumber() + " 已成功签收，交接单已自动完成。"
            );

            createNotification(
                    handover.getGiver(),
                    "Logistics_Delivered",
                    "接收方已签收包裹",
                    "物流单号 " + logistics.getTrackingNumber() + " 的包裹已被接收方签收，交接单已自动完成。"
            );

            logger.info("物流签收自动完成交接, handoverId: {}, trackingNumber: {}",
                    handover.getId(), logistics.getTrackingNumber());
        }
    }

    private void createNotification(User user, String type, String title, String content) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setStatus("Unread");
        notificationRepository.save(notification);
    }

    private void validatePermission(Logistics logistics, String username) {
        GearHandover handover = logistics.getHandover();
        if (handover == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "物流信息关联的交接单不存在");
        }
        if (!handover.getGiver().getUsername().equals(username)
                && !handover.getReceiver().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作此物流信息");
        }
    }

    private Logistics getLogisticsEntity(Long id) {
        return logisticsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "物流信息不存在"));
    }

    private ExpressCompanyDTO findExpressCompanyByCode(String code) {
        if (code == null) return null;
        return expressCompanies.stream()
                .filter(c -> code.equals(c.getCode()))
                .findFirst()
                .orElse(null);
    }

    private LogisticsDTO toDTO(Logistics logistics) {
        List<LogisticsStatusLogDTO> statusLogs = logistics.getStatusLogs() != null
                ? logistics.getStatusLogs().stream()
                .sorted(Comparator.comparing(LogisticsStatusLog::getOccurredAt))
                .map(this::toStatusLogDTO)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return new LogisticsDTO(
                logistics.getId(),
                logistics.getHandover() != null ? logistics.getHandover().getId() : null,
                logistics.getTrackingNumber(),
                logistics.getExpressCompanyCode(),
                logistics.getExpressCompanyName(),
                logistics.getCurrentStatus(),
                logistics.getCurrentLocation(),
                logistics.getEstimatedDeliveryTime(),
                logistics.getActualDeliveryTime(),
                logistics.getSenderName(),
                logistics.getSenderPhone(),
                logistics.getSenderAddress(),
                logistics.getReceiverName(),
                logistics.getReceiverPhone(),
                logistics.getReceiverAddress(),
                logistics.getLastSyncTime(),
                logistics.getSyncStatus(),
                logistics.getSyncFailCount(),
                logistics.getRemark(),
                logistics.getCreatedAt(),
                logistics.getUpdatedAt(),
                statusLogs
        );
    }

    private LogisticsStatusLogDTO toStatusLogDTO(LogisticsStatusLog log) {
        return new LogisticsStatusLogDTO(
                log.getId(),
                log.getStatusCode(),
                log.getStatusName(),
                log.getLocation(),
                log.getDescription(),
                log.getOccurredAt(),
                log.getIsSignatureRequired(),
                log.getOperatorName(),
                log.getOperatorPhone()
        );
    }

    private LogisticsSyncLogDTO toSyncLogDTO(LogisticsSyncLog log) {
        return new LogisticsSyncLogDTO(
                log.getId(),
                log.getLogistics() != null ? log.getLogistics().getId() : null,
                log.getTrackingNumber(),
                log.getExpressCompanyCode(),
                log.getSyncType(),
                log.getOperatorId(),
                log.getIsSuccess(),
                log.getErrorCode(),
                log.getErrorMessage(),
                log.getSyncStartTime(),
                log.getSyncEndTime(),
                log.getCreatedAt()
        );
    }

    private PageResponse<LogisticsDTO> toPageResponse(Page<Logistics> page) {
        List<LogisticsDTO> dtoList = page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return new PageResponse<>(
                dtoList,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
