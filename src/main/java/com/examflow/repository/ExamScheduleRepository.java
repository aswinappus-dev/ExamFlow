package com.examflow.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.examflow.model.ExamSchedule;

@Repository
public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Integer> {

    // Finds the next confirmed exam for student/invigilator view
    Optional<ExamSchedule> findFirstByRandomizationConfirmedIsTrueAndSlotStartTimeAfterOrderBySlotStartTimeAsc(LocalDateTime currentTime);

    // Finds the next unconfirmed exam for the admin panel
    Optional<ExamSchedule> findFirstByRandomizationConfirmedIsFalseAndSlotStartTimeAfterOrderBySlotStartTimeAsc(LocalDateTime currentTime);
    
    // Finds old exams for cleanup
    List<ExamSchedule> findBySlotEndTimeBefore(LocalDateTime time);
}

