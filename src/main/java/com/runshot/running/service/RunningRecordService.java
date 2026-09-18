package com.runshot.running.service;

import com.runshot.running.dto.RunningRecordCreateRequest;
import com.runshot.running.dto.RunningRecordUpdateRequest;
import com.runshot.running.entity.RunningRecord;
import com.runshot.running.repository.RunningRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RunningRecordService {

    private final RunningRecordRepository runningRecordRepository;

    public List<RunningRecord> getRuns() {
        return runningRecordRepository.findAll();
    }

    public RunningRecord createRun(
            RunningRecordCreateRequest request
    ) {

        RunningRecord runningRecord =
                RunningRecord.create(
                        request.distance(),
                        request.runningTime(),
                        request.runningDate(),
                        request.content()
                );

        return runningRecordRepository.save(runningRecord);
    }

    public RunningRecord getRun(Long id) {
        return runningRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("러닝 기록이 없습니다."));
    }

    public RunningRecord updateRun(
            Long id,
            RunningRecordUpdateRequest request
    ) {

        RunningRecord runningRecord =
                runningRecordRepository.findById(id)
                        .orElseThrow(
                                () -> new IllegalArgumentException("러닝 기록이 없습니다.")
                        );

        runningRecord.update(
                request.distance(),
                request.runningTime(),
                request.runningDate(),
                request.content()
        );

        return runningRecordRepository.save(runningRecord);
    }
    
    public void deleteRun(Long id) {

        RunningRecord runningRecord =
                runningRecordRepository.findById(id)
                        .orElseThrow(
                                () -> new IllegalArgumentException("러닝 기록이 없습니다.")
                        );

        runningRecordRepository.delete(runningRecord);
    }

}