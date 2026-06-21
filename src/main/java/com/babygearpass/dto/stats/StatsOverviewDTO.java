package com.babygearpass.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsOverviewDTO {

    private Long totalUsers;
    private Long totalGearItems;
    private Long totalHandovers;
    private Long totalStories;
    private Long availableItems;
    private Long reservedItems;
    private Long handedOverItems;
    private Long archivedItems;
}
