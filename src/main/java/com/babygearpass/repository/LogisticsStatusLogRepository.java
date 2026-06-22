package com.babygearpass.repository;

import com.babygearpass.entity.LogisticsStatusLog;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogisticsStatusLogRepository extends JpaRepository<LogisticsStatusLog, Long> {

    List<LogisticsStatusLog> findByLogisticsId(Long logisticsId, Sort sort);

    List<LogisticsStatusLog> findByLogisticsIdOrderByOccurredAtDesc(Long logisticsId);

    void deleteByLogisticsId(Long logisticsId);
}
