package com.runshot.running.controller;

import com.runshot.running.dto.*;
import com.runshot.running.service.RunningRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/runs")
@RequiredArgsConstructor
public class RunningRecordController {
    private final RunningRecordService runningRecordService;

    @GetMapping
    public List<RunningRecordResponse> getRuns(@AuthenticationPrincipal Long userId) {
        return runningRecordService.getRuns(userId);
    }

    @GetMapping("/{id}")
    public RunningRecordResponse getRun(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return runningRecordService.getRun(id, userId);
    }

    @GetMapping("/users/{requestedUserId}")
    public List<RunningRecordResponse> getRunsByUser(@PathVariable Long requestedUserId,
                                                   @AuthenticationPrincipal Long userId) {
        if (!requestedUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 기록만 조회할 수 있습니다.");
        }
        return runningRecordService.getRuns(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RunningRecordResponse createRun(@AuthenticationPrincipal Long userId,
                                           @Valid @RequestBody RunningRecordCreateRequest request) {
        return runningRecordService.createRun(userId, request);
    }

    @PutMapping("/{id}")
    public RunningRecordResponse updateRun(@PathVariable Long id, @AuthenticationPrincipal Long userId,
                                           @Valid @RequestBody RunningRecordUpdateRequest request) {
        return runningRecordService.updateRun(id, userId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRun(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        runningRecordService.deleteRun(id, userId);
    }
}
