package com.babygearpass.service;

import com.babygearpass.dto.qualitycheck.*;
import com.babygearpass.entity.GearItem;
import com.babygearpass.entity.Notification;
import com.babygearpass.entity.QualityCheck;
import com.babygearpass.entity.QualityCheckMaterial;
import com.babygearpass.entity.User;
import com.babygearpass.repository.GearItemRepository;
import com.babygearpass.repository.NotificationRepository;
import com.babygearpass.repository.QualityCheckMaterialRepository;
import com.babygearpass.repository.QualityCheckRepository;
import com.babygearpass.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QualityCheckService {

    private final QualityCheckRepository qualityCheckRepository;
    private final QualityCheckMaterialRepository qualityCheckMaterialRepository;
    private final GearItemRepository gearItemRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public Page<QualityCheckDTO> getAllQualityChecks(String status, Pageable pageable) {
        Page<QualityCheck> checks;
        if (status != null && !status.isBlank()) {
            checks = qualityCheckRepository.findByStatus(status, pageable);
        } else {
            checks = qualityCheckRepository.findAll(pageable);
        }
        return checks.map(this::toDTO);
    }

    public QualityCheckDTO getQualityCheckById(Long id) {
        QualityCheck check = qualityCheckRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "质检记录不存在"));
        return toDTO(check);
    }

    public Page<QualityCheckDTO> getQualityChecksByGearItem(Long gearItemId, Pageable pageable) {
        return qualityCheckRepository.findByGearItemId(gearItemId, pageable).map(this::toDTO);
    }

    public QualityCheckDTO getLatestQualityCheckByGearItem(Long gearItemId) {
        return qualityCheckRepository.findTopByGearItemIdOrderByCreatedAtDesc(gearItemId)
                .map(this::toDTO)
                .orElse(null);
    }

    public Page<QualityCheckDTO> getMyQualityChecks(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        return qualityCheckRepository.findBySubmitterId(user.getId(), pageable).map(this::toDTO);
    }

    public Page<QualityCheckDTO> getReviewQualityChecks(String reviewerUsername, Pageable pageable) {
        User reviewer = userRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        return qualityCheckRepository.findByReviewerId(reviewer.getId(), pageable).map(this::toDTO);
    }

    @Transactional
    public QualityCheckDTO submitQualityCheck(String username, QualityCheckRequest request) {
        User submitter = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        GearItem gearItem = gearItemRepository.findById(request.getGearItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用品不存在"));

        if (!gearItem.getOwner().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权为此用品提交质检");
        }

        QualityCheck existingPending = qualityCheckRepository.findTopByGearItemIdOrderByCreatedAtDesc(gearItem.getId())
                .orElse(null);
        if (existingPending != null && "Pending".equals(existingPending.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该用品已有待审核的质检记录");
        }

        QualityCheck check = new QualityCheck();
        check.setGearItem(gearItem);
        check.setSubmitter(submitter);
        check.setStatus("Pending");
        check.setRemark(request.getRemark());

        List<QualityCheckMaterial> materials = new ArrayList<>();
        if (request.getMaterials() != null && !request.getMaterials().isEmpty()) {
            for (QualityCheckMaterialRequest matReq : request.getMaterials()) {
                QualityCheckMaterial material = new QualityCheckMaterial();
                material.setQualityCheck(check);
                material.setMaterialType(matReq.getMaterialType());
                material.setFileName(matReq.getFileName());
                material.setFileUrl(matReq.getFileUrl());
                material.setFileSize(matReq.getFileSize());
                material.setDescription(matReq.getDescription());
                materials.add(material);
            }
        }
        check.setMaterials(materials);

        qualityCheckRepository.save(check);

        gearItem.setQualityCheckStatus("Pending");
        gearItemRepository.save(gearItem);

        return toDTO(check);
    }

    @Transactional
    public QualityCheckDTO reviewQualityCheck(Long id, String reviewerUsername, QualityCheckReviewRequest request) {
        QualityCheck check = qualityCheckRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "质检记录不存在"));

        if (!"Pending".equals(check.getStatus()) && !"SupplementRequested".equals(check.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该质检记录当前状态不可审核");
        }

        User reviewer = userRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        if (!"ADMIN".equals(reviewer.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只有管理员可以执行审核操作");
        }

        String status = request.getStatus();
        if (!"Approved".equals(status) && !"Rejected".equals(status) && !"SupplementRequested".equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的审核状态");
        }

        if ("Rejected".equals(status) || "SupplementRequested".equals(status)) {
            if (request.getRejectReason() == null || request.getRejectReason().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "审核不通过或要求补充材料时必须填写原因");
            }
            check.setRejectReason(request.getRejectReason());
        }

        if ("SupplementRequested".equals(status)) {
            if (request.getSupplementDeadline() == null) {
                check.setSupplementDeadline(LocalDateTime.now().plusDays(7));
            } else {
                check.setSupplementDeadline(request.getSupplementDeadline());
            }
        } else {
            check.setSupplementDeadline(null);
        }

        check.setReviewer(reviewer);
        check.setStatus(status);
        check.setReviewedAt(LocalDateTime.now());
        check.setQualityScore(request.getQualityScore());
        if (request.getRemark() != null) {
            check.setRemark(request.getRemark());
        }

        GearItem gearItem = check.getGearItem();
        gearItem.setQualityCheckStatus(status);
        if ("Approved".equals(status)) {
            gearItem.setStatus("Available");
            gearItem.setQualityScore(request.getQualityScore());
        } else if ("Rejected".equals(status)) {
            gearItem.setStatus("PendingQualityCheck");
            gearItem.setQualityScore(null);
        } else if ("SupplementRequested".equals(status)) {
            gearItem.setStatus("PendingQualityCheck");
            gearItem.setQualityScore(null);
        }
        gearItemRepository.save(gearItem);

        qualityCheckRepository.save(check);

        sendQualityCheckNotification(check, status, request.getRejectReason(), request.getSupplementDeadline());

        return toDTO(check);
    }

    @Transactional
    public QualityCheckDTO supplementMaterials(Long id, String username, QualityCheckSupplementRequest request) {
        QualityCheck check = qualityCheckRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "质检记录不存在"));

        if (!check.getSubmitter().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作此质检记录");
        }

        if (!"SupplementRequested".equals(check.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前状态无需补充材料");
        }

        if (check.getSupplementDeadline() != null && check.getSupplementDeadline().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "补充材料截止时间已过，请重新提交质检申请");
        }

        for (QualityCheckMaterialRequest matReq : request.getMaterials()) {
            QualityCheckMaterial material = new QualityCheckMaterial();
            material.setQualityCheck(check);
            material.setMaterialType(matReq.getMaterialType());
            material.setFileName(matReq.getFileName());
            material.setFileUrl(matReq.getFileUrl());
            material.setFileSize(matReq.getFileSize());
            material.setDescription(matReq.getDescription());
            qualityCheckMaterialRepository.save(material);
        }

        if (request.getRemark() != null) {
            check.setRemark(request.getRemark());
        }
        check.setStatus("Pending");
        check.setRejectReason(null);
        check.setSupplementDeadline(null);

        qualityCheckRepository.save(check);

        GearItem gearItem = check.getGearItem();
        gearItem.setQualityCheckStatus("Pending");
        gearItemRepository.save(gearItem);

        return toDTO(check);
    }

    @Transactional
    public void deleteQualityCheck(Long id, String username) {
        QualityCheck check = qualityCheckRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "质检记录不存在"));

        if (!check.getSubmitter().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权删除此质检记录");
        }

        if (!"Pending".equals(check.getStatus()) && !"Rejected".equals(check.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "仅待审核或已拒绝的质检记录可删除");
        }

        qualityCheckRepository.delete(check);
    }

    public List<QualityCheckMaterialDTO> getMaterialsByQualityCheckId(Long qualityCheckId) {
        return qualityCheckMaterialRepository.findByQualityCheckId(qualityCheckId)
                .stream()
                .map(this::toMaterialDTO)
                .collect(Collectors.toList());
    }

    private void sendQualityCheckNotification(QualityCheck check, String status, String rejectReason, LocalDateTime supplementDeadline) {
        User user = check.getSubmitter();
        String gearTitle = check.getGearItem().getTitle();
        String type;
        String title;
        StringBuilder content = new StringBuilder();

        if ("Approved".equals(status)) {
            type = "QualityCheckApproved";
            title = "质检审核通过";
            content.append("您的用品 \"").append(gearTitle).append("\" 的质检申请已审核通过。");
            if (check.getQualityScore() != null) {
                content.append(" 质量评分：").append(check.getQualityScore()).append("分。");
            }
            content.append(" 该用品已获得质检标识，将在搜索结果中优先展示。");
        } else if ("Rejected".equals(status)) {
            type = "QualityCheckRejected";
            title = "质检审核不通过";
            content.append("您的用品 \"").append(gearTitle).append("\" 的质检申请未通过审核。");
            content.append("\n\n不通过原因：").append(rejectReason);
            content.append("\n\n请根据原因补充或修改材料后重新提交质检申请。");
        } else if ("SupplementRequested".equals(status)) {
            type = "QualityCheckSupplement";
            title = "请补充质检材料";
            content.append("您的用品 \"").append(gearTitle).append("\" 的质检申请需要补充材料。");
            content.append("\n\n补充原因：").append(rejectReason);
            if (supplementDeadline != null) {
                content.append("\n\n请在 ").append(supplementDeadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append(" 前完成补充，逾期需重新提交申请。");
            }
        } else {
            return;
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content.toString());
        notification.setQualityCheck(check);
        notification.setStatus("Unread");
        notificationRepository.save(notification);
    }

    private QualityCheckDTO toDTO(QualityCheck check) {
        QualityCheckDTO dto = new QualityCheckDTO();
        dto.setId(check.getId());
        dto.setGearItemId(check.getGearItem().getId());
        dto.setGearItemTitle(check.getGearItem().getTitle());
        dto.setSubmitterId(check.getSubmitter().getId());
        dto.setSubmitterName(check.getSubmitter().getUsername());
        if (check.getReviewer() != null) {
            dto.setReviewerId(check.getReviewer().getId());
            dto.setReviewerName(check.getReviewer().getUsername());
        }
        dto.setStatus(check.getStatus());
        dto.setRejectReason(check.getRejectReason());
        dto.setSupplementDeadline(check.getSupplementDeadline());
        dto.setRemark(check.getRemark());
        dto.setQualityScore(check.getQualityScore());
        dto.setCreatedAt(check.getCreatedAt());
        dto.setUpdatedAt(check.getUpdatedAt());
        dto.setReviewedAt(check.getReviewedAt());
        if (check.getMaterials() != null) {
            dto.setMaterials(check.getMaterials().stream()
                    .map(this::toMaterialDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private QualityCheckMaterialDTO toMaterialDTO(QualityCheckMaterial material) {
        return new QualityCheckMaterialDTO(
                material.getId(),
                material.getMaterialType(),
                material.getFileName(),
                material.getFileUrl(),
                material.getFileSize(),
                material.getDescription(),
                material.getCreatedAt()
        );
    }
}
