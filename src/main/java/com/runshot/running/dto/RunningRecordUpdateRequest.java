package com.runshot.running.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.*;

public record RunningRecordUpdateRequest(
        @NotNull @DecimalMin("0.01") @DecimalMax("1000.0") Double distance,
        @NotNull @Positive @Max(604800) Integer runningTime,
        @NotNull @PastOrPresent LocalDate runningDate,
        @Size(max = 500) String content
) {
}
