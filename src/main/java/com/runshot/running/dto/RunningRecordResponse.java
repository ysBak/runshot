package com.runshot.running.dto;

import com.runshot.running.entity.RunningRecord;
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
    public static RunningRecordResponse from(RunningRecord record) {
        return new RunningRecordResponse(record.getId(), record.getUser().getId(),
                record.getUser().getNickname(), record.getDistance(), record.getRunningTime(),
                record.getPace(), record.getRunningDate(), record.getContent());
    }
}
