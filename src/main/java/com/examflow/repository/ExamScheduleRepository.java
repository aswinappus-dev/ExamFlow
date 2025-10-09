package com.examflow.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.examflow.model.ExamSchedule;

@Repository
public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Integer> {

    // Finds the next CONFIRMED exam for student/invigilator views and scheduler
    Optional<ExamSchedule> findFirstByRandomizationConfirmedIsTrueAndSlotStartTimeAfterOrderBySlotStartTimeAsc(LocalDateTime currentTime);

    // Finds ALL UNCONFIRMED exams for the admin panel
    List<ExamSchedule> findAllByRandomizationConfirmedIsFalseAndSlotStartTimeAfterOrderBySlotStartTimeAsc(LocalDateTime currentTime);
    
    // Finds old exams for the cleanup task
    List<ExamSchedule> findBySlotEndTimeBefore(LocalDateTime time);
}

