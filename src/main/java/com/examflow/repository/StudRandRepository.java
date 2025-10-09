package com.examflow.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.examflow.model.StudRand;

@Repository
public interface StudRandRepository extends JpaRepository<StudRand, Long> {

    // Finds a single student's arrangement by their register number
    Optional<StudRand> findByRegisterNo(String registerNo);

    // Finds all student arrangements for a specific hall
    List<StudRand> findByExamhallNo(String examhallNo); // THIS METHOD WAS MISSING
    List<StudRand> findByExamhallNoOrderBySeatNoAsc(String examhallNo);
}

