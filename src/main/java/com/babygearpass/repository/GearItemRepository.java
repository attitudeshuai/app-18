package com.babygearpass.repository;

import com.babygearpass.entity.GearItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GearItemRepository extends JpaRepository<GearItem, Long> {

    Page<GearItem> findByOwnerId(Long ownerId, Pageable pageable);

    Page<GearItem> findByCategoryId(Long categoryId, Pageable pageable);

    Page<GearItem> findByStatus(String status, Pageable pageable);

    Page<GearItem> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("SELECT g FROM GearItem g WHERE LOWER(g.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY CASE WHEN g.qualityCheckStatus = 'Approved' THEN 1 ELSE 2 END ASC, " +
           "CASE WHEN g.qualityScore IS NOT NULL THEN g.qualityScore ELSE 0 END DESC, " +
           "g.createdAt DESC")
    Page<GearItem> findByKeywordWithQualityPriority(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT g FROM GearItem g WHERE g.status = :status " +
           "ORDER BY CASE WHEN g.qualityCheckStatus = 'Approved' THEN 1 ELSE 2 END ASC, " +
           "CASE WHEN g.qualityScore IS NOT NULL THEN g.qualityScore ELSE 0 END DESC, " +
           "g.createdAt DESC")
    Page<GearItem> findByStatusWithQualityPriority(@Param("status") String status, Pageable pageable);

    @Query("SELECT g FROM GearItem g WHERE g.category.id = :categoryId " +
           "ORDER BY CASE WHEN g.qualityCheckStatus = 'Approved' THEN 1 ELSE 2 END ASC, " +
           "CASE WHEN g.qualityScore IS NOT NULL THEN g.qualityScore ELSE 0 END DESC, " +
           "g.createdAt DESC")
    Page<GearItem> findByCategoryIdWithQualityPriority(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT g FROM GearItem g " +
           "ORDER BY CASE WHEN g.qualityCheckStatus = 'Approved' THEN 1 ELSE 2 END ASC, " +
           "CASE WHEN g.qualityScore IS NOT NULL THEN g.qualityScore ELSE 0 END DESC, " +
           "g.createdAt DESC")
    Page<GearItem> findAllWithQualityPriority(Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT CAST(g.createdAt AS date) AS date, COUNT(g) AS count FROM GearItem g WHERE g.createdAt >= :startDate GROUP BY CAST(g.createdAt AS date) ORDER BY date")
    List<Object[]> countByDate(@Param("startDate") LocalDateTime startDate);
}
