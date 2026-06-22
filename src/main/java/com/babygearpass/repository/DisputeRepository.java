package com.babygearpass.repository;

import com.babygearpass.entity.Dispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    Page<Dispute> findByStatus(String status, Pageable pageable);

    Page<Dispute> findByHandoverId(Long handoverId, Pageable pageable);

    Page<Dispute> findByInitiatorId(Long initiatorId, Pageable pageable);

    Page<Dispute> findByArbitratorId(Long arbitratorId, Pageable pageable);

    Optional<Dispute> findByHandoverIdAndStatusIn(Long handoverId, List<String> statuses);

    List<Dispute> findByHandoverId(Long handoverId);

    long countByStatus(String status);
}
