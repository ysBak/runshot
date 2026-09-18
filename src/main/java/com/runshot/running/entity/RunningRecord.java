package com.runshot.running.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "running_records")
public class RunningRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double distance;
    // km

    private Integer runningTime;
    // 초

    private Integer pace;
    // 1km당 초

    private LocalDate runningDate;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static RunningRecord create(
            Double distance,
            Integer runningTime,
            LocalDate runningDate,
            String content
    ) {
        RunningRecord record = new RunningRecord();

        record.distance = distance;
        record.runningTime = runningTime;
        record.pace = (int) (runningTime / distance);
        record.runningDate = runningDate;
        record.content = content;

        return record;
    }

    public void update(
            Double distance,
            Integer runningTime,
            LocalDate runningDate,
            String content
    ) {
        this.distance = distance;
        this.runningTime = runningTime;
        this.pace = (int) Math.round(runningTime / distance);
        this.runningDate = runningDate;
        this.content = content;
    }

}