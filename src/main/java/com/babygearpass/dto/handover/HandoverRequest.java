package com.babygearpass.dto.handover;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandoverRequest {

    @NotNull
    private Long gearItemId;

    @NotNull
    private Long receiverId;

    private LocalDate handoverDate;

    private String location;

    private String note;
}
