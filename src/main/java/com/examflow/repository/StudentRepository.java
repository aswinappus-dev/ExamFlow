package com.examflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.examflow.model.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    List<Student> findByRegisterNoContaining(String branchCode);

    /**
     * Finds all students whose register number starts with a specific prefix.
     * This is used to find students by year and branch (e.g., "CEC22AD").
     * @param prefix The year and branch code prefix.
     * @return A list of matching Student entities.
     */
    List<Student> findByRegisterNoStartingWith(String prefix);
}
