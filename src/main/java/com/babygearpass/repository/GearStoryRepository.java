package com.babygearpass.repository;

import com.babygearpass.entity.GearStory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GearStoryRepository extends JpaRepository<GearStory, Long> {

    Page<GearStory> findByUserId(Long userId, Pageable pageable);

    Page<GearStory> findByGearItemId(Long gearItemId, Pageable pageable);

    Page<GearStory> findByContentContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("SELECT CAST(s.createdAt AS date) AS date, COUNT(s) AS count FROM GearStory s WHERE s.createdAt >= :startDate GROUP BY CAST(s.createdAt AS date) ORDER BY date")
    List<Object[]> countByDate(@Param("startDate") LocalDateTime startDate);
}
