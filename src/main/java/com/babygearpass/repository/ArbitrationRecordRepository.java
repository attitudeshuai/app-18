package com.babygearpass.repository;

import com.babygearpass.entity.ArbitrationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArbitrationRecordRepository extends JpaRepository<ArbitrationRecord, Long> {

    List<ArbitrationRecord> findByDisputeIdOrderByCreatedAtDesc(Long disputeId);

    Page<ArbitrationRecord> findByDisputeId(Long disputeId, Pageable pageable);

    List<ArbitrationRecord> findByDisputeIdAndIsPublicTrueOrderByCreatedAtDesc(Long disputeId);
}
