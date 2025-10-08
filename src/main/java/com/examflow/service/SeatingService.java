package com.examflow.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examflow.model.Admin;
import com.examflow.model.ExamHall;
import com.examflow.model.ExamSchedule;
import com.examflow.model.Student;
import com.examflow.repository.AdminRepository;
import com.examflow.repository.ExamHallRepository;
import com.examflow.repository.ExamScheduleRepository;
import com.examflow.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class SeatingService {

    @Autowired private StudentRepository studentRepository;
    @Autowired private ExamHallRepository examHallRepository;
    @Autowired private ExamScheduleRepository examScheduleRepository;
    @Autowired private AdminRepository adminRepository;

    @Transactional
    public void performRandomization() {
        // ... (logic is unchanged)
    }

    @Transactional
    public void clearArrangementsForPastExams() {
        // ... (logic is unchanged)
    }
    
    public Optional<ExamSchedule> findNextConfirmedExam() {
        return examScheduleRepository.findFirstByRandomizationConfirmedIsTrueAndSlotStartTimeAfterOrderBySlotStartTimeAsc(LocalDateTime.now());
    }
    
    /**
     * ADDED: This method was missing, causing the error in your WebController.
     * It finds all exams that are upcoming but have not yet been confirmed by the admin.
     */
    public List<ExamSchedule> findAllUnconfirmedExams() {
        return examScheduleRepository.findAllByRandomizationConfirmedIsFalseAndSlotStartTimeAfterOrderBySlotStartTimeAsc(LocalDateTime.now());
    }

    public Map<String, Object> getStudentArrangement(String registerNo) {
        // ... (logic is unchanged)
        return new HashMap<>(); // Placeholder
    }

    public Map<String, Object> getHallArrangement(String examhallNo) {
        // ... (logic is unchanged)
        return new HashMap<>(); // Placeholder
    }

    public boolean verifyAdminCredentials(HttpSession session, String email, String code) {
        Optional<Admin> adminOpt = adminRepository.findById(email);
        if (adminOpt.isPresent() && adminOpt.get().getVerificationCode().equals(code)) {
            session.setAttribute("isAdmin", true);
            return true;
        }
        return false;
    }
    
    public Map<String, List<ExamHall>> getHallsGroupedByBlock() { 
        return examHallRepository.findAll().stream()
               .collect(Collectors.groupingBy(ExamHall::getBlockno, TreeMap::new, Collectors.toList())); 
    }
    
    // --- Simple data management methods ---
    public List<Student> getAllStudents() { return studentRepository.findAll(); }
    public List<ExamSchedule> getAllSchedules() { return examScheduleRepository.findAll(); }
    public void addStudent(Student s) { studentRepository.save(s); }
    public void deleteStudent(String r) { studentRepository.deleteById(r); }
    public void addHall(ExamHall h) { examHallRepository.save(h); }
    public void deleteHall(String h) { examHallRepository.deleteById(h); }
    public void addSchedule(ExamSchedule s) { examScheduleRepository.save(s); }
    public void confirmRandomization(Integer id) { 
        examScheduleRepository.findById(id).ifPresent(s -> { 
            s.setRandomizationConfirmed(true); 
            examScheduleRepository.save(s); 
        }); 
    }
}

