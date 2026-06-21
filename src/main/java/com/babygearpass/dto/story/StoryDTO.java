package com.babygearpass.dto.story;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoryDTO {

    private Long id;
    private Long gearItemId;
    private String gearItemTitle;
    private Long userId;
    private String username;
    private String content;
    private String photos;
    private LocalDateTime createdAt;
}
