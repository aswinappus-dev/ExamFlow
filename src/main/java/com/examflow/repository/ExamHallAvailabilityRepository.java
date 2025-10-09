package com.examflow.repository;

import com.examflow.model.ExamHallAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamHallAvailabilityRepository extends JpaRepository<ExamHallAvailability, Long> {

    /**
     * Finds all availability records for a given exam schedule.
     */
    List<ExamHallAvailability> findByScheduleId(Integer scheduleId);

    /**
     * Finds all halls marked as unavailable for a specific schedule.
     */
    List<ExamHallAvailability> findByScheduleIdAndIsAvailableFalse(Integer scheduleId);
}
