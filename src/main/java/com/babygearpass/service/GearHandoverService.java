package com.babygearpass.service;

import com.babygearpass.dto.handover.HandoverDTO;
import com.babygearpass.dto.handover.HandoverRequest;
import com.babygearpass.dto.handover.HandoverStatusRequest;
import com.babygearpass.dto.logistics.LogisticsDTO;
import com.babygearpass.dto.logistics.LogisticsStatusLogDTO;
import com.babygearpass.entity.*;
import com.babygearpass.repository.GearHandoverRepository;
import com.babygearpass.repository.GearItemRepository;
import com.babygearpass.repository.QualityCheckRepository;
import com.babygearpass.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GearHandoverService {

    private final GearHandoverRepository gearHandoverRepository;
    private final GearItemRepository gearItemRepository;
    private final UserRepository userRepository;
    private final QualityCheckRepository qualityCheckRepository;

    public Page<HandoverDTO> getAllHandovers(String keyword, String status, Long gearItemId, Pageable pageable) {
        Page<GearHandover> handovers;

        if (status != null && !status.isBlank()) {
            handovers = gearHandoverRepository.findByStatus(status, pageable);
        } else if (gearItemId != null) {
            handovers = gearHandoverRepository.findByGearItemId(gearItemId, pageable);
        } else {
            handovers = gearHandoverRepository.findAll(pageable);
        }

        return handovers.map(this::toDTO);
    }

    public Page<HandoverDTO> getMyHandovers(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        Page<GearHandover> asGiver = gearHandoverRepository.findByGiverId(user.getId(), pageable);
        return asGiver.map(this::toDTO);
    }

    public HandoverDTO getHandoverById(Long id) {
        GearHandover handover = gearHandoverRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "交接记录不存在"));
        return toDTO(handover);
    }

    public HandoverDTO createHandover(String username, HandoverRequest request) {
        User giver = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "赠送方用户不存在"));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "接收方用户不存在"));

        GearItem gearItem = gearItemRepository.findById(request.getGearItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用品不存在"));

        if (!gearItem.getOwner().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作此用品");
        }

        GearHandover handover = new GearHandover();
        handover.setGearItem(gearItem);
        handover.setGiver(giver);
        handover.setReceiver(receiver);
        handover.setHandoverDate(request.getHandoverDate());
        handover.setLocation(request.getLocation());
        handover.setNote(request.getNote());

        QualityCheck latestQC = qualityCheckRepository.findTopByGearItemIdOrderByCreatedAtDesc(gearItem.getId()).orElse(null);
        if (latestQC != null && "Approved".equals(latestQC.getStatus())) {
            handover.setQualityCheck(latestQC);
        }

        gearHandoverRepository.save(handover);
        return toDTO(handover);
    }

    public HandoverDTO updateHandover(Long id, String username, HandoverRequest request) {
        GearHandover handover = gearHandoverRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "交接记录不存在"));

        if (!handover.getGiver().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作此交接记录");
        }

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "接收方用户不存在"));

        GearItem gearItem = gearItemRepository.findById(request.getGearItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用品不存在"));

        handover.setGearItem(gearItem);
        handover.setReceiver(receiver);
        handover.setHandoverDate(request.getHandoverDate());
        handover.setLocation(request.getLocation());
        handover.setNote(request.getNote());

        gearHandoverRepository.save(handover);
        return toDTO(handover);
    }

    public void deleteHandover(Long id, String username) {
        GearHandover handover = gearHandoverRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "交接记录不存在"));

        if (!handover.getGiver().getUsername().equals(username) && !handover.getReceiver().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作此交接记录");
        }

        gearHandoverRepository.delete(handover);
    }

    public HandoverDTO updateHandoverStatus(Long id, String username, HandoverStatusRequest request) {
        GearHandover handover = gearHandoverRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "交接记录不存在"));

        if (!handover.getGiver().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作此交接记录");
        }

        handover.setStatus(request.getStatus());
        gearHandoverRepository.save(handover);
        return toDTO(handover);
    }

    private HandoverDTO toDTO(GearHandover handover) {
        Long qcId = null;
        String qcStatus = null;
        Integer qcScore = null;
        boolean certified = false;

        if (handover.getQualityCheck() != null) {
            qcId = handover.getQualityCheck().getId();
            qcStatus = handover.getQualityCheck().getStatus();
            qcScore = handover.getQualityCheck().getQualityScore();
            certified = "Approved".equals(qcStatus);
        }

        LogisticsDTO logisticsDTO = null;
        if (handover.getLogistics() != null) {
            logisticsDTO = toLogisticsDTO(handover.getLogistics());
        }

        return new HandoverDTO(
                handover.getId(),
                handover.getGearItem().getId(),
                handover.getGearItem().getTitle(),
                handover.getGiver().getId(),
                handover.getGiver().getUsername(),
                handover.getReceiver().getId(),
                handover.getReceiver().getUsername(),
                qcId,
                qcStatus,
                qcScore,
                certified,
                handover.getHandoverDate(),
                handover.getLocation(),
                handover.getStatus(),
                handover.getNote(),
                handover.getCreatedAt(),
                handover.getEscrowStatus(),
                handover.getEscrowStartAt(),
                handover.getEscrowEndAt(),
                handover.getFrozenPoints(),
                handover.getConfirmedByReceiver(),
                handover.getHasDispute(),
                handover.getIsFrozen(),
                logisticsDTO
        );
    }

    private LogisticsDTO toLogisticsDTO(Logistics logistics) {
        List<LogisticsStatusLogDTO> statusLogs = logistics.getStatusLogs() != null
                ? logistics.getStatusLogs().stream()
                .sorted(Comparator.comparing(LogisticsStatusLog::getOccurredAt))
                .map(this::toLogisticsStatusLogDTO)
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

    private LogisticsStatusLogDTO toLogisticsStatusLogDTO(LogisticsStatusLog log) {
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
}
