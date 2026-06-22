package com.babygearpass.repository;

import com.babygearpass.entity.LogisticsSyncLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LogisticsSyncLogRepository extends JpaRepository<LogisticsSyncLog, Long> {

    Page<LogisticsSyncLog> findByLogisticsId(Long logisticsId, Pageable pageable);

    List<LogisticsSyncLog> findByLogisticsIdOrderByCreatedAtDesc(Long logisticsId);

    Page<LogisticsSyncLog> findByIsSuccessFalse(Pageable pageable);

    Page<LogisticsSyncLog> findByCreatedAtAfter(LocalDateTime dateTime, Pageable pageable);

    void deleteByCreatedAtBefore(LocalDateTime dateTime);
}
