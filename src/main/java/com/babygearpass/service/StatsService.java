package com.babygearpass.service;

import com.babygearpass.dto.stats.StatsOverviewDTO;
import com.babygearpass.dto.stats.StatsTrendDTO;
import com.babygearpass.repository.GearHandoverRepository;
import com.babygearpass.repository.GearItemRepository;
import com.babygearpass.repository.GearStoryRepository;
import com.babygearpass.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final GearItemRepository gearItemRepository;
    private final GearHandoverRepository gearHandoverRepository;
    private final GearStoryRepository gearStoryRepository;
    private final UserRepository userRepository;

    public StatsOverviewDTO getOverview() {
        long totalUsers = userRepository.count();
        long totalGearItems = gearItemRepository.count();
        long totalHandovers = gearHandoverRepository.count();
        long totalStories = gearStoryRepository.count();

        long availableItems = gearItemRepository.countByStatus("Available");
        long reservedItems = gearItemRepository.countByStatus("Reserved");
        long handedOverItems = gearItemRepository.countByStatus("HandedOver");
        long archivedItems = gearItemRepository.countByStatus("Archived");

        return new StatsOverviewDTO(
                totalUsers, totalGearItems, totalHandovers, totalStories,
                availableItems, reservedItems, handedOverItems, archivedItems
        );
    }

    public List<StatsTrendDTO> getTrend(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();

        Map<LocalDate, Long> gearItemCounts = gearItemRepository.countByDate(startDateTime)
                .stream()
                .collect(Collectors.toMap(
                        row -> toLocalDate(row[0]),
                        row -> ((Number) row[1]).longValue()
                ));

        Map<LocalDate, Long> handoverCounts = gearHandoverRepository.countByDate(startDate)
                .stream()
                .collect(Collectors.toMap(
                        row -> toLocalDate(row[0]),
                        row -> ((Number) row[1]).longValue()
                ));

        Map<LocalDate, Long> storyCounts = gearStoryRepository.countByDate(startDateTime)
                .stream()
                .collect(Collectors.toMap(
                        row -> toLocalDate(row[0]),
                        row -> ((Number) row[1]).longValue()
                ));

        List<StatsTrendDTO> trends = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            trends.add(new StatsTrendDTO(
                    date.toString(),
                    gearItemCounts.getOrDefault(date, 0L),
                    handoverCounts.getOrDefault(date, 0L),
                    storyCounts.getOrDefault(date, 0L)
            ));
        }

        return trends;
    }

    private LocalDate toLocalDate(Object dateObj) {
        if (dateObj instanceof java.sql.Date) {
            return ((java.sql.Date) dateObj).toLocalDate();
        } else if (dateObj instanceof LocalDateTime) {
            return ((LocalDateTime) dateObj).toLocalDate();
        } else if (dateObj instanceof LocalDate) {
            return (LocalDate) dateObj;
        } else {
            return LocalDate.parse(dateObj.toString());
        }
    }
}
