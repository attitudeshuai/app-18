package com.babygearpass.repository;

import com.babygearpass.entity.CreditRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditRecordRepository extends JpaRepository<CreditRecord, Long> {

    Page<CreditRecord> findByUserId(Long userId, Pageable pageable);

    List<CreditRecord> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<CreditRecord> findByRelatedDisputeId(Long disputeId);
}
