package com.babygearpass.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsTrendDTO {

    private String date;
    private Long gearItemCount;
    private Long handoverCount;
    private Long storyCount;
}
