package com.runshot.running.repository;

import com.runshot.running.entity.RunningRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunningRecordRepository
        extends JpaRepository<RunningRecord, Long> {
}