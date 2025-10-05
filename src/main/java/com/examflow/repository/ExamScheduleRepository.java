package com.examflow.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.examflow.model.ExamSchedule;

@Repository
public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {

    /**
     * Finds the next upcoming exam schedule that has been confirmed for randomization
     * and is scheduled to start after the current time. It returns the one with the
     * earliest start time.
     * * @param currentTime The current time to check against.
     * @return An Optional containing the next confirmed ExamSchedule, or empty if none is found.
     */
    Optional<ExamSchedule> findFirstByRandomizationConfirmedIsTrueAndSlotStartTimeIsAfterOrderBySlotStartTimeAsc(LocalDateTime currentTime);
}

