package com.examflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.examflow.model.ExamHall;

// This gives us methods like findAll(), findById(), save(), etc., for our ExamHall entity.
// The primary key for ExamHall is a String (examhallno).
@Repository
public interface ExamHallRepository extends JpaRepository<ExamHall, String> {
    // We don't need any custom methods here for now.
    // The default methods provided by JpaRepository are enough.
}
