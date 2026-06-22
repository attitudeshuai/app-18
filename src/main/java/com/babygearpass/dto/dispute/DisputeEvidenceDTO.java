package com.babygearpass.dto.dispute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisputeEvidenceDTO {

    private Long id;
    private Long disputeId;
    private Long uploaderId;
    private String uploaderName;
    private String type;
    private String title;
    private String description;
    private String fileUrl;
    private LocalDateTime createdAt;
}
