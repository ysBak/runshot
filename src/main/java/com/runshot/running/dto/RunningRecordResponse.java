package com.runshot.running.dto;

import java.time.LocalDate;

public record RunningRecordResponse(
        Long id,
        Long userId,
        String nickname,
        Double distance,
        Integer runningTime,
        Integer pace,
        LocalDate runningDate,
        String content
) {
}