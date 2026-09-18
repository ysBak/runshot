package com.runshot.running.dto;

import java.time.LocalDate;

public record RunningRecordUpdateRequest(
        Double distance,
        Integer runningTime,
        LocalDate runningDate,
        String content
) {
}