package com.babygearpass.service;

import com.babygearpass.dto.credit.CreditRecordDTO;
import com.babygearpass.entity.CreditRecord;
import com.babygearpass.entity.User;
import com.babygearpass.repository.CreditRecordRepository;
import com.babygearpass.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CreditService {

    private final UserRepository userRepository;
    private final CreditRecordRepository creditRecordRepository;

    public Page<CreditRecordDTO> getMyCreditRecords(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        Page<CreditRecord> records = creditRecordRepository.findByUserId(user.getId(), pageable);
        return records.map(this::toDTO);
    }

    public Page<CreditRecordDTO> getUserCreditRecords(Long userId, Pageable pageable) {
        Page<CreditRecord> records = creditRecordRepository.findByUserId(userId, pageable);
        return records.map(this::toDTO);
    }

    @Transactional
    public void changeCreditScore(Long userId, int scoreChange, String type, String reason,
                                  Long relatedHandoverId, Long relatedDisputeId, Long operatorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        int beforeScore = user.getCreditScore();
        int afterScore = Math.max(0, Math.min(100, beforeScore + scoreChange));
        int actualChange = afterScore - beforeScore;

        if (actualChange == 0) {
            return;
        }

        user.setCreditScore(afterScore);
        user.setCreditLevel(determineCreditLevel(afterScore));
        userRepository.save(user);

        CreditRecord record = new CreditRecord();
        record.setUser(user);
        record.setType(type);
        record.setScoreChange(actualChange);
        record.setBeforeScore(beforeScore);
        record.setAfterScore(afterScore);
        record.setReason(reason);
        record.setRelatedHandoverId(relatedHandoverId);
        record.setRelatedDisputeId(relatedDisputeId);
        record.setOperatorId(operatorId);
        creditRecordRepository.save(record);
    }

    @Transactional
    public void freezePoints(Long userId, int points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        if (user.getAvailablePoints() < points) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "可用积分不足");
        }

        user.setAvailablePoints(user.getAvailablePoints() - points);
        user.setFrozenPoints(user.getFrozenPoints() + points);
        userRepository.save(user);
    }

    @Transactional
    public void unfreezePoints(Long userId, int points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        if (user.getFrozenPoints() < points) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "冻结积分不足");
        }

        user.setFrozenPoints(user.getFrozenPoints() - points);
        user.setAvailablePoints(user.getAvailablePoints() + points);
        userRepository.save(user);
    }

    @Transactional
    public void transferFrozenPoints(Long fromUserId, Long toUserId, int points) {
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "转出用户不存在"));
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "转入用户不存在"));

        if (fromUser.getFrozenPoints() < points) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "冻结积分不足");
        }

        fromUser.setFrozenPoints(fromUser.getFrozenPoints() - points);
        toUser.setAvailablePoints(toUser.getAvailablePoints() + points);

        userRepository.save(fromUser);
        userRepository.save(toUser);
    }

    private String determineCreditLevel(int score) {
        if (score >= 90) {
            return "Excellent";
        } else if (score >= 80) {
            return "Good";
        } else if (score >= 60) {
            return "Fair";
        } else if (score >= 40) {
            return "Poor";
        } else {
            return "Bad";
        }
    }

    private CreditRecordDTO toDTO(CreditRecord record) {
        return new CreditRecordDTO(
                record.getId(),
                record.getUser().getId(),
                record.getUser().getUsername(),
                record.getRelatedHandoverId(),
                record.getRelatedDisputeId(),
                record.getType(),
                record.getScoreChange(),
                record.getBeforeScore(),
                record.getAfterScore(),
                record.getReason(),
                record.getOperatorId(),
                record.getCreatedAt()
        );
    }
}
