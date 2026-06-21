package com.babygearpass.dto.handover;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandoverDTO {

    private Long id;
    private Long gearItemId;
    private String gearItemTitle;
    private Long giverId;
    private String giverName;
    private Long receiverId;
    private String receiverName;
    private LocalDate handoverDate;
    private String location;
    private String status;
    private String note;
}
