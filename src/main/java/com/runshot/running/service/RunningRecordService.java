package com.runshot.running.service;

import com.runshot.running.dto.*;
import com.runshot.running.entity.RunningRecord;
import com.runshot.running.repository.RunningRecordRepository;
import com.runshot.user.entity.User;
import com.runshot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RunningRecordService {
    private final RunningRecordRepository runningRecordRepository;
    private final UserRepository userRepository;

    public List<RunningRecordResponse> getRuns(Long userId) {
        return runningRecordRepository.findByUserIdOrderByRunningDateDescIdDesc(userId)
                .stream().map(RunningRecordResponse::from).toList();
    }

    public RunningRecordResponse getRun(Long id, Long userId) {
        return RunningRecordResponse.from(findOwned(id, userId));
    }

    @Transactional
    public RunningRecordResponse createRun(Long userId, RunningRecordCreateRequest request) {
        if (request.userId() != null && !request.userId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "다른 사용자의 기록을 만들 수 없습니다.");
        }
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "다시 로그인해 주세요."));
        RunningRecord record = RunningRecord.create(user, request.distance(), request.runningTime(),
                request.runningDate(), request.content());
        return RunningRecordResponse.from(runningRecordRepository.save(record));
    }

    @Transactional
    public RunningRecordResponse updateRun(Long id, Long userId, RunningRecordUpdateRequest request) {
        RunningRecord record = findOwned(id, userId);
        record.update(request.distance(), request.runningTime(), request.runningDate(), request.content());
        return RunningRecordResponse.from(record);
    }

    @Transactional
    public void deleteRun(Long id, Long userId) {
        runningRecordRepository.delete(findOwned(id, userId));
    }

    private RunningRecord findOwned(Long id, Long userId) {
        return runningRecordRepository.findByIdAndUserId(id, userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "러닝 기록을 찾을 수 없습니다."));
    }
}
