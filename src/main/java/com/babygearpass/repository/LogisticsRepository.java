package com.babygearpass.repository;

import com.babygearpass.entity.Logistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LogisticsRepository extends JpaRepository<Logistics, Long> {

    Optional<Logistics> findByHandoverId(Long handoverId);

    Optional<Logistics> findByTrackingNumber(String trackingNumber);

    Optional<Logistics> findByTrackingNumberAndExpressCompanyCode(String trackingNumber, String expressCompanyCode);

    Page<Logistics> findByCurrentStatus(String currentStatus, Pageable pageable);

    Page<Logistics> findBySyncStatus(String syncStatus, Pageable pageable);

    @Query("SELECT l FROM Logistics l WHERE l.handover.giver.id = :userId OR l.handover.receiver.id = :userId")
    Page<Logistics> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT l FROM Logistics l WHERE l.syncStatus = 'Failed' AND l.syncFailCount < :maxFailCount")
    List<Logistics> findFailedSyncLogistics(@Param("maxFailCount") Integer maxFailCount);

    @Query("SELECT l FROM Logistics l WHERE l.currentStatus NOT IN ('Delivered', 'Signed') AND l.lastSyncTime < :lastSyncTime")
    List<Logistics> findLogisticsNeedSync(@Param("lastSyncTime") LocalDateTime lastSyncTime);

    @Query("SELECT l.expressCompanyCode, COUNT(l) FROM Logistics l GROUP BY l.expressCompanyCode")
    List<Object[]> countByExpressCompany();
}
