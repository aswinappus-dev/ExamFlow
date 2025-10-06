package com.examflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.examflow.model.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, String> {
}
