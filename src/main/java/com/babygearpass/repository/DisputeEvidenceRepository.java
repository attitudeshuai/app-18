package com.babygearpass.repository;

import com.babygearpass.entity.DisputeEvidence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisputeEvidenceRepository extends JpaRepository<DisputeEvidence, Long> {

    List<DisputeEvidence> findByDisputeIdOrderByCreatedAtDesc(Long disputeId);

    Page<DisputeEvidence> findByDisputeId(Long disputeId, Pageable pageable);

    List<DisputeEvidence> findByDisputeIdAndUploaderId(Long disputeId, Long uploaderId);
}
