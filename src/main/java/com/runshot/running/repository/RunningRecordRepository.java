package com.runshot.running.repository;

import com.runshot.running.entity.RunningRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RunningRecordRepository
        extends JpaRepository<RunningRecord, Long> {

    List<RunningRecord> findByUserIdOrderByRunningDateDescIdDesc(Long userId);
    Optional<RunningRecord> findByIdAndUserId(Long id, Long userId);
}
