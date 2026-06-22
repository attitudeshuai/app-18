package com.babygearpass.repository;

import com.babygearpass.entity.QualityCheck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QualityCheckRepository extends JpaRepository<QualityCheck, Long> {

    Page<QualityCheck> findByGearItemId(Long gearItemId, Pageable pageable);

    Page<QualityCheck> findBySubmitterId(Long submitterId, Pageable pageable);

    Page<QualityCheck> findByStatus(String status, Pageable pageable);

    Page<QualityCheck> findByReviewerId(Long reviewerId, Pageable pageable);

    Optional<QualityCheck> findTopByGearItemIdOrderByCreatedAtDesc(Long gearItemId);

    List<QualityCheck> findByGearItemId(Long gearItemId);

    @Query("SELECT q FROM QualityCheck q WHERE q.status = :status AND q.supplementDeadline < :now")
    List<QualityCheck> findExpiredSupplementChecks(@Param("status") String status, @Param("now") LocalDateTime now);

    long countByStatus(String status);

    @Query("SELECT CAST(q.createdAt AS date) AS date, COUNT(q) AS count FROM QualityCheck q WHERE q.createdAt >= :startDate GROUP BY CAST(q.createdAt AS date) ORDER BY date")
    List<Object[]> countByDate(@Param("startDate") LocalDateTime startDate);
}
