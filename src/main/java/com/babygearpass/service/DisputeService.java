package com.babygearpass.service;

import com.babygearpass.dto.dispute.ArbitrationRecordDTO;
import com.babygearpass.dto.dispute.ArbitrationRequest;
import com.babygearpass.dto.dispute.DisputeDTO;
import com.babygearpass.dto.dispute.DisputeEvidenceDTO;
import com.babygearpass.dto.dispute.DisputeEvidenceRequest;
import com.babygearpass.dto.dispute.DisputeRequest;
import com.babygearpass.entity.ArbitrationRecord;
import com.babygearpass.entity.Dispute;
import com.babygearpass.entity.DisputeEvidence;
import com.babygearpass.entity.GearHandover;
import com.babygearpass.entity.User;
import com.babygearpass.repository.ArbitrationRecordRepository;
import com.babygearpass.repository.DisputeEvidenceRepository;
import com.babygearpass.repository.DisputeRepository;
import com.babygearpass.repository.GearHandoverRepository;
import com.babygearpass.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final DisputeEvidenceRepository disputeEvidenceRepository;
    private final ArbitrationRecordRepository arbitrationRecordRepository;
    private final GearHandoverRepository gearHandoverRepository;
    private final UserRepository userRepository;
    private final CreditService creditService;
    private final EscrowService escrowService;

    private static final List<String> ACTIVE_DISPUTE_STATUSES = Arrays.asList("Pending", "Reviewing", "Frozen");

    public Page<DisputeDTO> getAllDisputes(String status, Pageable pageable) {
        Page<Dispute> disputes;
        if (status != null && !status.isBlank()) {
            disputes = disputeRepository.findByStatus(status, pageable);
        } else {
            disputes = disputeRepository.findAll(pageable);
        }
        return disputes.map(this::toDTO);
    }

    public Page<DisputeDTO> getMyDisputes(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        return disputeRepository.findByUserIdAsParty(user.getId(), pageable)
                .map(this::toDTO);
    }

    public DisputeDTO getDisputeById(Long id, String username) {
        Dispute dispute = disputeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "纠纷不存在"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        boolean isParty = dispute.getHandover().getGiver().getId().equals(user.getId())
                || dispute.getHandover().getReceiver().getId().equals(user.getId());

        if (!isParty && !"ADMIN".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权查看此纠纷");
        }

        return toDTO(dispute);
    }

    @Transactional
    public DisputeDTO initiateDispute(String username, DisputeRequest request) {
        User initiator = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        GearHandover handover = gearHandoverRepository.findById(request.getHandoverId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "交接记录不存在"));

        boolean isParty = handover.getGiver().getId().equals(initiator.getId())
                || handover.getReceiver().getId().equals(initiator.getId());

        if (!isParty) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只有交接双方可以发起纠纷");
        }

        Dispute existingActive = disputeRepository
                .findByHandoverIdAndStatusIn(handover.getId(), ACTIVE_DISPUTE_STATUSES)
                .orElse(null);

        if (existingActive != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该交接已有进行中的纠纷");
        }

        Dispute dispute = new Dispute();
        dispute.setHandover(handover);
        dispute.setInitiator(initiator);
        dispute.setDisputeType(request.getDisputeType());
        dispute.setDescription(request.getDescription());
        dispute.setStatus("Pending");
        disputeRepository.save(dispute);

        handover.setEscrowStatus("Disputed");
        handover.setHasDispute(true);
        gearHandoverRepository.save(handover);

        addArbitrationRecord(dispute, initiator, "Initiate", "用户发起纠纷", true);

        return toDTO(dispute);
    }

    @Transactional
    public DisputeEvidenceDTO uploadEvidence(Long disputeId, String username, DisputeEvidenceRequest request) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "纠纷不存在"));

        User uploader = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        boolean isParty = dispute.getHandover().getGiver().getId().equals(uploader.getId())
                || dispute.getHandover().getReceiver().getId().equals(uploader.getId());

        if (!isParty) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只有纠纷双方可以上传证据");
        }

        if (!ACTIVE_DISPUTE_STATUSES.contains(dispute.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "纠纷当前状态不支持上传证据");
        }

        DisputeEvidence evidence = new DisputeEvidence();
        evidence.setDispute(dispute);
        evidence.setUploader(uploader);
        evidence.setType(request.getType());
        evidence.setTitle(request.getTitle());
        evidence.setDescription(request.getDescription());
        evidence.setFileUrl(request.getFileUrl());
        disputeEvidenceRepository.save(evidence);

        addArbitrationRecord(dispute, uploader, "UploadEvidence",
                "上传证据: " + request.getTitle(), true);

        return toEvidenceDTO(evidence);
    }

    @Transactional
    public DisputeDTO adminAcceptDispute(Long disputeId, String adminUsername) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "纠纷不存在"));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "管理员不存在"));

        if (!"ADMIN".equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "需要管理员权限");
        }

        if (!"Pending".equals(dispute.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "纠纷状态不是待受理");
        }

        dispute.setStatus("Reviewing");
        dispute.setArbitrator(admin);
        disputeRepository.save(dispute);

        addArbitrationRecord(dispute, admin, "Accept", "管理员受理纠纷", true);

        return toDTO(dispute);
    }

    @Transactional
    public DisputeDTO adminFreezeDispute(Long disputeId, String adminUsername, String reason) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "纠纷不存在"));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "管理员不存在"));

        if (!"ADMIN".equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "需要管理员权限");
        }

        if (!ACTIVE_DISPUTE_STATUSES.contains(dispute.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "纠纷当前状态无法冻结");
        }

        dispute.setStatus("Frozen");
        disputeRepository.save(dispute);

        escrowService.adminFreezeEscrow(dispute.getHandover().getId(), adminUsername, reason);

        addArbitrationRecord(dispute, admin, "Freeze",
                "冻结纠纷流程: " + (reason != null ? reason : ""), true);

        return toDTO(dispute);
    }

    @Transactional
    public DisputeDTO adminUnfreezeDispute(Long disputeId, String adminUsername) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "纠纷不存在"));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "管理员不存在"));

        if (!"ADMIN".equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "需要管理员权限");
        }

        if (!"Frozen".equals(dispute.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "纠纷未处于冻结状态");
        }

        dispute.setStatus("Reviewing");
        disputeRepository.save(dispute);

        escrowService.adminUnfreezeEscrow(dispute.getHandover().getId(), adminUsername);

        addArbitrationRecord(dispute, admin, "Unfreeze", "解除纠纷冻结", true);

        return toDTO(dispute);
    }

    @Transactional
    public DisputeDTO arbitrate(Long disputeId, String adminUsername, ArbitrationRequest request) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "纠纷不存在"));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "管理员不存在"));

        if (!"ADMIN".equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "需要管理员权限");
        }

        if ("Resolved".equals(dispute.getStatus()) || "Closed".equals(dispute.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "纠纷已处理完毕");
        }

        GearHandover handover = dispute.getHandover();

        dispute.setArbitrationResult(request.getArbitrationResult());
        dispute.setArbitrationOpinion(request.getArbitrationOpinion());
        dispute.setArbitratedAt(LocalDateTime.now());
        dispute.setStatus("Resolved");

        int giverChange = request.getGiverCreditChange() != null ? request.getGiverCreditChange() : 0;
        int receiverChange = request.getReceiverCreditChange() != null ? request.getReceiverCreditChange() : 0;

        dispute.setGiverCreditChange(giverChange);
        dispute.setReceiverCreditChange(receiverChange);
        dispute.setPointsHandleResult(request.getPointsHandleResult());

        if (giverChange != 0) {
            creditService.changeCreditScore(
                    handover.getGiver().getId(),
                    giverChange,
                    "DisputeArbitration",
                    "纠纷仲裁信用调整: " + request.getArbitrationResult(),
                    handover.getId(),
                    dispute.getId(),
                    admin.getId()
            );
        }

        if (receiverChange != 0) {
            creditService.changeCreditScore(
                    handover.getReceiver().getId(),
                    receiverChange,
                    "DisputeArbitration",
                    "纠纷仲裁信用调整: " + request.getArbitrationResult(),
                    handover.getId(),
                    dispute.getId(),
                    admin.getId()
            );
        }

        handlePointsAfterArbitration(dispute, handover, request.getPointsHandleResult());

        if ("GiverWin".equals(request.getArbitrationResult())
                || "ReceiverWin".equals(request.getArbitrationResult())
                || "Partial".equals(request.getArbitrationResult())) {
            handover.setEscrowStatus("Completed");
            handover.setStatus("Completed");
            handover.setIsFrozen(false);
            gearHandoverRepository.save(handover);
        }

        disputeRepository.save(dispute);

        addArbitrationRecord(dispute, admin, "Arbitrate",
                "仲裁结果: " + request.getArbitrationResult() + " - "
                        + (request.getArbitrationOpinion() != null ? request.getArbitrationOpinion() : ""),
                true);

        return toDTO(dispute);
    }

    private void handlePointsAfterArbitration(Dispute dispute, GearHandover handover, String pointsResult) {
        int frozenPoints = handover.getFrozenPoints() != null ? handover.getFrozenPoints() : 0;
        if (frozenPoints <= 0) {
            return;
        }

        switch (pointsResult != null ? pointsResult : "ReturnToGiver") {
            case "ReturnToGiver":
                creditService.unfreezePoints(handover.getGiver().getId(), frozenPoints);
                break;
            case "TransferToReceiver":
                creditService.transferFrozenPoints(
                        handover.getGiver().getId(),
                        handover.getReceiver().getId(),
                        frozenPoints
                );
                break;
            case "Partial":
                int half = frozenPoints / 2;
                if (half > 0) {
                    creditService.transferFrozenPoints(
                            handover.getGiver().getId(),
                            handover.getReceiver().getId(),
                            half
                    );
                }
                int remaining = frozenPoints - half;
                if (remaining > 0) {
                    creditService.unfreezePoints(handover.getGiver().getId(), remaining);
                }
                break;
            default:
                creditService.unfreezePoints(handover.getGiver().getId(), frozenPoints);
                break;
        }

        handover.setFrozenPoints(0);
    }

    public List<DisputeEvidenceDTO> getDisputeEvidences(Long disputeId, String username) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "纠纷不存在"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        boolean isParty = dispute.getHandover().getGiver().getId().equals(user.getId())
                || dispute.getHandover().getReceiver().getId().equals(user.getId());

        if (!isParty && !"ADMIN".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权查看证据");
        }

        return disputeEvidenceRepository.findByDisputeIdOrderByCreatedAtDesc(disputeId)
                .stream()
                .map(this::toEvidenceDTO)
                .collect(Collectors.toList());
    }

    public List<ArbitrationRecordDTO> getArbitrationRecords(Long disputeId, String username) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "纠纷不存在"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        boolean isParty = dispute.getHandover().getGiver().getId().equals(user.getId())
                || dispute.getHandover().getReceiver().getId().equals(user.getId());

        List<ArbitrationRecord> records;
        if ("ADMIN".equals(user.getRole())) {
            records = arbitrationRecordRepository.findByDisputeIdOrderByCreatedAtDesc(disputeId);
        } else if (isParty) {
            records = arbitrationRecordRepository.findByDisputeIdAndIsPublicTrueOrderByCreatedAtDesc(disputeId);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权查看仲裁记录");
        }

        return records.stream()
                .map(this::toRecordDTO)
                .collect(Collectors.toList());
    }

    private void addArbitrationRecord(Dispute dispute, User operator, String action, String remark, boolean isPublic) {
        ArbitrationRecord record = new ArbitrationRecord();
        record.setDispute(dispute);
        record.setOperator(operator);
        record.setAction(action);
        record.setRemark(remark);
        record.setIsPublic(isPublic);
        arbitrationRecordRepository.save(record);
    }

    private DisputeDTO toDTO(Dispute dispute) {
        List<DisputeEvidenceDTO> evidenceDTOs = null;
        if (dispute.getEvidences() != null) {
            evidenceDTOs = dispute.getEvidences().stream()
                    .map(this::toEvidenceDTO)
                    .collect(Collectors.toList());
        }

        List<ArbitrationRecordDTO> recordDTOs = null;
        if (dispute.getArbitrationRecords() != null) {
            recordDTOs = dispute.getArbitrationRecords().stream()
                    .map(this::toRecordDTO)
                    .collect(Collectors.toList());
        }

        return new DisputeDTO(
                dispute.getId(),
                dispute.getHandover().getId(),
                dispute.getInitiator().getId(),
                dispute.getInitiator().getUsername(),
                dispute.getStatus(),
                dispute.getDisputeType(),
                dispute.getDescription(),
                dispute.getArbitrator() != null ? dispute.getArbitrator().getId() : null,
                dispute.getArbitrator() != null ? dispute.getArbitrator().getUsername() : null,
                dispute.getArbitrationResult(),
                dispute.getArbitrationOpinion(),
                dispute.getGiverCreditChange(),
                dispute.getReceiverCreditChange(),
                dispute.getPointsHandleResult(),
                dispute.getArbitratedAt(),
                dispute.getCreatedAt(),
                dispute.getUpdatedAt(),
                evidenceDTOs,
                recordDTOs
        );
    }

    private DisputeEvidenceDTO toEvidenceDTO(DisputeEvidence evidence) {
        return new DisputeEvidenceDTO(
                evidence.getId(),
                evidence.getDispute().getId(),
                evidence.getUploader().getId(),
                evidence.getUploader().getUsername(),
                evidence.getType(),
                evidence.getTitle(),
                evidence.getDescription(),
                evidence.getFileUrl(),
                evidence.getCreatedAt()
        );
    }

    private ArbitrationRecordDTO toRecordDTO(ArbitrationRecord record) {
        return new ArbitrationRecordDTO(
                record.getId(),
                record.getDispute().getId(),
                record.getOperator().getId(),
                record.getOperator().getUsername(),
                record.getAction(),
                record.getRemark(),
                record.getIsPublic(),
                record.getCreatedAt()
        );
    }
}
