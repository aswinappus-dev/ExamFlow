package com.examflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.examflow.model.ExamGroup;

@Repository
public interface ExamGroupRepository extends JpaRepository<ExamGroup, Long> {

    /**
     * Finds all student groups assigned to a specific exam schedule.
     * @param scheduleId The ID of the exam_schedule.
     * @return A list of ExamGroup entities.
     */
    List<ExamGroup> findByScheduleId(Integer scheduleId);
}
