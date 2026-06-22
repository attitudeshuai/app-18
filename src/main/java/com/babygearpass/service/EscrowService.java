package com.babygearpass.service;

import com.babygearpass.config.EscrowConfig;
import com.babygearpass.dto.escrow.ConfirmReceiptRequest;
import com.babygearpass.dto.escrow.EscrowDTO;
import com.babygearpass.dto.escrow.StartEscrowRequest;
import com.babygearpass.entity.GearHandover;
import com.babygearpass.entity.User;
import com.babygearpass.repository.GearHandoverRepository;
import com.babygearpass.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EscrowService {

    private final GearHandoverRepository gearHandoverRepository;
    private final UserRepository userRepository;
    private final CreditService creditService;
    private final EscrowConfig escrowConfig;

    private static final List<String> ACTIVE_ESCROW_STATUSES = Arrays.asList("Active", "Disputed", "Frozen");

    public EscrowDTO getEscrowInfo(Long handoverId, String username) {
        GearHandover handover = gearHandoverRepository.findById(handoverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "交接记录不存在"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        if (!handover.getGiver().getId().equals(user.getId())
                && !handover.getReceiver().getId().equals(user.getId())
                && !"ADMIN".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权查看此担保信息");
        }

        return toDTO(handover);
    }

    @Transactional
    public EscrowDTO startEscrow(Long handoverId, String username, StartEscrowRequest request) {
        GearHandover handover = gearHandoverRepository.findById(handoverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "交接记录不存在"));

        if (!handover.getGiver().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只有赠送方可以发起担保");
        }

        if (ACTIVE_ESCROW_STATUSES.contains(handover.getEscrowStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "担保已在进行中");
        }

        if (!"InProgress".equals(handover.getStatus()) && !"Pending".equals(handover.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前交接状态不支持发起担保");
        }

        int points = request.getPoints() != null ? request.getPoints() : 0;

        if (points > 0) {
            creditService.freezePoints(handover.getGiver().getId(), points);
        }

        LocalDateTime now = LocalDateTime.now();
        handover.setEscrowStatus("Active");
        handover.setEscrowStartAt(now);
        handover.setEscrowEndAt(now.plusHours(escrowConfig.getDefaultPeriodHours()));
        handover.setFrozenPoints(points);
        handover.setConfirmedByReceiver(false);
        handover.setConfirmedAt(null);

        gearHandoverRepository.save(handover);
        return toDTO(handover);
    }

    @Transactional
    public EscrowDTO confirmReceipt(Long handoverId, String username, ConfirmReceiptRequest request) {
        GearHandover handover = gearHandoverRepository.findById(handoverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "交接记录不存在"));

        if (!handover.getReceiver().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只有接收方可以确认收货");
        }

        if (!"Active".equals(handover.getEscrowStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "担保状态不支持确认收货");
        }

        if (handover.getConfirmedByReceiver()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "已确认收货，请勿重复操作");
        }

        handover.setConfirmedByReceiver(true);
        handover.setConfirmedAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(request.getAcceptQuality())) {
            completeEscrow(handover, "ReceiverConfirmed");
        } else {
            handover.setEscrowStatus("Disputed");
            handover.setHasDispute(true);
        }

        gearHandoverRepository.save(handover);
        return toDTO(handover);
    }

    @Transactional
    public void completeEscrow(GearHandover handover, String reason) {
        if (handover.getFrozenPoints() != null && handover.getFrozenPoints() > 0) {
            creditService.unfreezePoints(handover.getGiver().getId(), handover.getFrozenPoints());
        }

        handover.setEscrowStatus("Completed");
        handover.setStatus("Completed");
        handover.setIsFrozen(false);

        creditService.changeCreditScore(
                handover.getGiver().getId(),
                2,
                "HandoverComplete",
                "交接完成，信用加分",
                handover.getId(),
                null,
                null
        );
        creditService.changeCreditScore(
                handover.getReceiver().getId(),
                2,
                "HandoverComplete",
                "交接完成，信用加分",
                handover.getId(),
                null,
                null
        );
    }

    @Transactional
    public EscrowDTO adminFreezeEscrow(Long handoverId, String adminUsername, String reason) {
        GearHandover handover = gearHandoverRepository.findById(handoverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "交接记录不存在"));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "管理员不存在"));

        if (!"ADMIN".equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "需要管理员权限");
        }

        if (!ACTIVE_ESCROW_STATUSES.contains(handover.getEscrowStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前状态无法冻结");
        }

        handover.setEscrowStatus("Frozen");
        handover.setIsFrozen(true);
        gearHandoverRepository.save(handover);

        return toDTO(handover);
    }

    @Transactional
    public EscrowDTO adminUnfreezeEscrow(Long handoverId, String adminUsername) {
        GearHandover handover = gearHandoverRepository.findById(handoverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "交接记录不存在"));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "管理员不存在"));

        if (!"ADMIN".equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "需要管理员权限");
        }

        if (!"Frozen".equals(handover.getEscrowStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "担保未处于冻结状态");
        }

        if (handover.getHasDispute()) {
            handover.setEscrowStatus("Disputed");
        } else {
            handover.setEscrowStatus("Active");
        }
        handover.setIsFrozen(false);
        gearHandoverRepository.save(handover);

        return toDTO(handover);
    }

    @Transactional
    @Scheduled(fixedRateString = "${escrow.check-interval-ms:60000}")
    public void processExpiredEscrows() {
        if (!escrowConfig.isAutoCompleteAfterPeriod()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<GearHandover> expiredHandovers = gearHandoverRepository.findAll().stream()
                .filter(h -> "Active".equals(h.getEscrowStatus()))
                .filter(h -> h.getEscrowEndAt() != null && h.getEscrowEndAt().isBefore(now))
                .filter(h -> Boolean.FALSE.equals(h.getHasDispute()))
                .toList();

        for (GearHandover handover : expiredHandovers) {
            completeEscrow(handover, "AutoExpired");
            gearHandoverRepository.save(handover);
        }
    }

    private EscrowDTO toDTO(GearHandover handover) {
        Long remainingSeconds = null;
        if (handover.getEscrowEndAt() != null
                && ACTIVE_ESCROW_STATUSES.contains(handover.getEscrowStatus())) {
            remainingSeconds = Duration.between(LocalDateTime.now(), handover.getEscrowEndAt()).getSeconds();
            if (remainingSeconds < 0) {
                remainingSeconds = 0L;
            }
        }

        return new EscrowDTO(
                handover.getId(),
                handover.getEscrowStatus(),
                handover.getEscrowStartAt(),
                handover.getEscrowEndAt(),
                handover.getFrozenPoints(),
                handover.getConfirmedByReceiver(),
                handover.getConfirmedAt(),
                handover.getHasDispute(),
                handover.getIsFrozen(),
                remainingSeconds
        );
    }
}
