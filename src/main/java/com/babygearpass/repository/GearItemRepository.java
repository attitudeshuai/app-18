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

    long countByStatus(String status);

    @Query("SELECT CAST(g.createdAt AS date) AS date, COUNT(g) AS count FROM GearItem g WHERE g.createdAt >= :startDate GROUP BY CAST(g.createdAt AS date) ORDER BY date")
    List<Object[]> countByDate(@Param("startDate") LocalDateTime startDate);
}
