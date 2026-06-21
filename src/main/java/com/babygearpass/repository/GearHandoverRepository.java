package com.babygearpass.repository;

import com.babygearpass.entity.GearHandover;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface GearHandoverRepository extends JpaRepository<GearHandover, Long> {

    Page<GearHandover> findByGiverId(Long giverId, Pageable pageable);

    Page<GearHandover> findByReceiverId(Long receiverId, Pageable pageable);

    Page<GearHandover> findByGearItemId(Long gearItemId, Pageable pageable);

    Page<GearHandover> findByStatus(String status, Pageable pageable);

    @Query("SELECT h.handoverDate AS date, COUNT(h) AS count FROM GearHandover h WHERE h.handoverDate >= :startDate GROUP BY h.handoverDate ORDER BY date")
    List<Object[]> countByDate(@Param("startDate") LocalDate startDate);
}
