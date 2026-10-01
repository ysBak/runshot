package com.runshot.running.controller;

import com.runshot.running.dto.RunningRecordCreateRequest;
import com.runshot.running.dto.RunningRecordUpdateRequest;
import com.runshot.running.entity.RunningRecord;
import com.runshot.running.service.RunningRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/runs")
@RequiredArgsConstructor
public class RunningRecordController {

    private final RunningRecordService runningRecordService;

    @GetMapping
    public List<RunningRecord> getRuns() {
        return runningRecordService.getRuns();
    }

    @GetMapping("/{id}")
    public RunningRecord getRun(@PathVariable Long id) {
        return runningRecordService.getRun(id);
    }

    @GetMapping("/users/{userId}")
    public List<RunningRecord> getRunsByUser(
            @PathVariable Long userId
    ) {
        return runningRecordService.getRunsByUser(userId);
    }


    @PostMapping
    public RunningRecord createRun(
            @Valid @RequestBody RunningRecordCreateRequest request
    ) {
        return runningRecordService.createRun(request);
    }

    @PutMapping("/{id}")
    public RunningRecord updateRun(
            @PathVariable Long id,
            @RequestBody RunningRecordUpdateRequest request
    ) {
        return runningRecordService.updateRun(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteRun(@PathVariable Long id) {
        runningRecordService.deleteRun(id);
    }

}