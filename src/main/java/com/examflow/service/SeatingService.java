package com.examflow.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.examflow.model.Admin;
import com.examflow.model.ExamHall;
import com.examflow.model.ExamSchedule;
import com.examflow.model.StudRand;
import com.examflow.model.Student;
import com.examflow.repository.AdminRepository;
import com.examflow.repository.ExamHallRepository;
import com.examflow.repository.ExamScheduleRepository;
import com.examflow.repository.StudRandRepository;
import com.examflow.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Service
public class SeatingService {

    @Autowired private StudentRepository studentRepository;
    @Autowired private ExamHallRepository examHallRepository;
    @Autowired private StudRandRepository studRandRepository;
    @Autowired private ExamScheduleRepository examScheduleRepository;
    @Autowired private AdminRepository adminRepository;
    private final Random random = new Random();

    // --- Data Retrieval Methods ---

    public Map<String, Object> getStudentArrangement(String registerNo) {
        Map<String, Object> result = new HashMap<>();
        Optional<ExamSchedule> nextExamOpt = findNextExam();
        
        if (studRandRepository.count() == 0) {
            String message = "Seating arrangement has not been generated yet. Please check back closer to the exam time.";
            if(nextExamOpt.isPresent()){
                message = "Seating arrangement will be available 2 hours before the exam starts on " + nextExamOpt.get().getSlotStartTime().toLocalDate() + ".";
            }
            result.put("message", message);
            return result;
        }

        Optional<StudRand> arrangement = studRandRepository.findByRegisterNo(registerNo);
        if (arrangement.isPresent()) {
            result.put("arrangement", arrangement.get());
        } else {
            result.put("message", "Your register number was not found in the current seating arrangement.");
        }
        return result;
    }

    public Map<String, Object> getHallArrangement(String examhallNo) {
        Map<String, Object> result = new HashMap<>();
        if (studRandRepository.count() == 0) {
            result.put("message", "Seating arrangement has not been generated yet.");
            return result;
        }
        result.put("arrangements", studRandRepository.findByExamhallNo(examhallNo));
        return result;
    }
    
    // --- The rest of your service methods ---
    
    @Transactional
    public void performRandomization() {
        studRandRepository.deleteAll();
        List<Student> allStudents = studentRepository.findAll();
        List<ExamHall> allHalls = examHallRepository.findAll();
        Collections.shuffle(allStudents);
        Map<String, List<Student>> studentsByBranch = allStudents.stream().collect(Collectors.groupingBy(Student::getBranch));
        int hallIndex = 0;
        for (ExamHall hall : allHalls) {
            if (hallIndex >= allHalls.size()) break;
            int capacity = hall.getCapacity();
            List<Student> assignedStudents = new ArrayList<>();
            for (List<Student> branchStudents : studentsByBranch.values()) {
                if (assignedStudents.size() >= capacity) break;
                int studentsToAdd = Math.min(branchStudents.size(), capacity - assignedStudents.size());
                if (studentsToAdd > 0) {
                    assignedStudents.addAll(branchStudents.subList(0, studentsToAdd));
                    branchStudents.subList(0, studentsToAdd).clear();
                }
            }
            for (Student student : assignedStudents) {
                StudRand arrangement = new StudRand();
                arrangement.setRegisterNo(student.getRegisterNo());
                arrangement.setExamhallNo(hall.getExamhallNo());
                arrangement.setBlockno(hall.getBlockno());
                studRandRepository.save(arrangement);
            }
            hallIndex++;
        }
    }

    @Transactional
    public void clearArrangementsForPastExams() {
        List<ExamSchedule> pastSchedules = examScheduleRepository.findBySlotEndTimeBefore(LocalDateTime.now().minusHours(4));
        if (!pastSchedules.isEmpty()) {
            studRandRepository.deleteAll();
        }
    }
    
    public boolean generateAndStoreVerificationCode(HttpSession session, String email) {
        Optional<Admin> adminOpt = adminRepository.findById(email);
        if (adminOpt.isEmpty()) { return false; }
        String code = generateRandomCode(6);
        session.setAttribute("verificationCode", code);
        session.setAttribute("codeTimestamp", LocalDateTime.now());
        System.out.println("Admin verification code for " + email + ": " + code);
        return true;
    }

    public boolean verifyAdminCode(HttpSession session, String code) {
        String storedCode = (String) session.getAttribute("verificationCode");
        LocalDateTime timestamp = (LocalDateTime) session.getAttribute("codeTimestamp");
        if (storedCode != null && timestamp != null && 
            timestamp.plusMinutes(10).isAfter(LocalDateTime.now()) &&
            storedCode.equals(code)) { 
            session.setAttribute("isAdmin", true); 
            return true;
        }
        return false;
    }
    
    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) { sb.append(chars.charAt(random.nextInt(chars.length()))); }
        return sb.toString();
    }
    
    public List<Student> getAllStudents() { return studentRepository.findAll(); }
    public void addStudent(Student student) { studentRepository.save(student); }
    public void deleteStudent(String registerNo) { studentRepository.deleteById(registerNo); }
    public List<ExamHall> getAllHalls() { return examHallRepository.findAll(); }
    public void addHall(ExamHall hall) { examHallRepository.save(hall); }
    public void deleteHall(String examhallNo) { examHallRepository.deleteById(examhallNo); }
    public List<ExamSchedule> getAllSchedules() { return examScheduleRepository.findAll(); }
    public void addSchedule(ExamSchedule schedule) { examScheduleRepository.save(schedule); }
    public void confirmRandomization(Integer scheduleId) {
        examScheduleRepository.findById(scheduleId).ifPresent(s -> {
            s.setRandomizationConfirmed(true);
            examScheduleRepository.save(s);
        });
    }
    public Optional<ExamSchedule> findNextExam() {
        return examScheduleRepository.findFirstByRandomizationConfirmedIsTrueAndSlotStartTimeIsAfterOrderBySlotStartTimeAsc(LocalDateTime.now());
    }
}

