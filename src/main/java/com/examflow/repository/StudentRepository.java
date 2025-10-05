package com.examflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.examflow.model.Student;

// JpaRepository gives us all the standard database methods for the Student entity.
// The primary key of the Student table is a String (register_no).
@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    // Spring Data JPA is smart. By naming a method like this, it will automatically
    // create a query to find all students whose register number contains a specific string.
    // We will use this to find all students of a certain branch, e.g., "AD", "CS".
    List<Student> findByRegisterNoContaining(String branchCode);
}
