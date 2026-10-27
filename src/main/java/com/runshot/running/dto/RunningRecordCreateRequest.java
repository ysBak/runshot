package com.runshot.running.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PastOrPresent;

public record RunningRecordCreateRequest(
        // 실제 소유자는 JWT에서 결정한다. 이전 요청의 userId는 일치 여부만 검증한다.
        @Positive
        Long userId,

        @NotNull
        @Positive
        @DecimalMin("0.01")
        @DecimalMax("1000.0")
        Double distance,

        @NotNull
        @Positive
        @Max(604800)
        Integer runningTime,

        @NotNull
        @PastOrPresent
        LocalDate runningDate,

        @Size(max = 500)
        String content
) {
}
