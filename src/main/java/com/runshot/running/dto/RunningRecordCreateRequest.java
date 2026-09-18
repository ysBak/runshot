package com.runshot.running.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RunningRecordCreateRequest(

        @NotNull
        @Positive
        Double distance,

        @NotNull
        @Positive
        Integer runningTime,

        @NotNull
        LocalDate runningDate,

        @Size(max = 500)
        String content
) {
}