package com.examflow.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.examflow.model.ExamSchedule;

@Repository
// CORRECTED: The ID type is now Integer to match your ExamSchedule.java entity.
public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Integer> {

    Optional<ExamSchedule> findFirstByRandomizationConfirmedIsTrueAndSlotStartTimeIsAfterOrderBySlotStartTimeAsc(LocalDateTime currentTime);
    
    // ADDED: This method is required by the SeatingService for cleanup.
    List<ExamSchedule> findBySlotEndTimeBefore(LocalDateTime time);
}

