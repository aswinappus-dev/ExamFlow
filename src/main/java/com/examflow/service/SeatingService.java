package com.examflow.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Transactional
    public void performRandomization() {
        studRandRepository.deleteAll();
        List<Student> allStudents = studentRepository.findAll();
        List<ExamHall> allHalls = examHallRepository.findAll();
        Collections.shuffle(allStudents);
        Map<String, List<Student>> studentsByBranch = allStudents.stream().collect(Collectors.groupingBy(Student::getBranch));
        for (ExamHall hall : allHalls) {
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
        }
    }

    @Transactional
    public void clearArrangementsForPastExams() {
        List<ExamSchedule> pastSchedules = examScheduleRepository.findBySlotEndTimeBefore(LocalDateTime.now().minusMinutes(4)); // For testing
        if (!pastSchedules.isEmpty()) {
            studRandRepository.deleteAll();
        }
    }

    public Optional<ExamSchedule> findNextExam() {
        return examScheduleRepository.findFirstByRandomizationConfirmedIsTrueAndSlotStartTimeAfterOrderBySlotStartTimeAsc(LocalDateTime.now());
    }
    
    public Optional<ExamSchedule> findNextUnconfirmedExam() {
        return examScheduleRepository.findFirstByRandomizationConfirmedIsFalseAndSlotStartTimeAfterOrderBySlotStartTimeAsc(LocalDateTime.now());
    }

    public Map<String, Object> getStudentArrangement(String registerNo) {
        Map<String, Object> result = new HashMap<>();
        var nextConfirmedExam = findNextExam();
        if (nextConfirmedExam.isEmpty()) {
            result.put("message", "No upcoming exam has been scheduled for randomization.");
            return result;
        }
        LocalDateTime revealTime = nextConfirmedExam.get().getSlotStartTime().minusMinutes(1);
        if (LocalDateTime.now().isBefore(revealTime)) {
            result.put("revealTime", revealTime);
            return result;
        }
        studRandRepository.findByRegisterNo(registerNo)
            .ifPresentOrElse(
                arrangement -> result.put("arrangement", arrangement),
                () -> result.put("message", "Your register number was not found.")
            );
        return result;
    }

    public Map<String, Object> getHallArrangement(String examhallNo) {
        Map<String, Object> result = new HashMap<>();
        var nextConfirmedExam = findNextExam();
        if (nextConfirmedExam.isEmpty()) {
            result.put("message", "No upcoming exam has been scheduled for randomization.");
            return result;
        }
        LocalDateTime revealTime = nextConfirmedExam.get().getSlotStartTime().minusMinutes(1);
        if (LocalDateTime.now().isBefore(revealTime)) {
            result.put("revealTime", revealTime);
            return result;
        }
        result.put("arrangements", studRandRepository.findByExamhallNo(examhallNo));
        return result;
    }

    public boolean verifyAdminCredentials(HttpSession session, String email, String code) {
        Optional<Admin> adminOpt = adminRepository.findById(email);
        if (adminOpt.isPresent() && adminOpt.get().getVerificationCode().equals(code)) {
            session.setAttribute("isAdmin", true);
            return true;
        }
        return false;
    }
    
    public Map<String, List<ExamHall>> getHallsGroupedByBlock() { return examHallRepository.findAll().stream().collect(Collectors.groupingBy(ExamHall::getBlockno)); }
    public List<Student> getAllStudents() { return studentRepository.findAll(); }
    public List<ExamHall> getAllHalls() { return examHallRepository.findAll(); }
    public List<ExamSchedule> getAllSchedules() { return examScheduleRepository.findAll(); }
    public void addStudent(Student s) { studentRepository.save(s); }
    public void deleteStudent(String r) { studentRepository.deleteById(r); }
    public void addHall(ExamHall h) { examHallRepository.save(h); }
    public void deleteHall(String h) { examHallRepository.deleteById(h); }
    public void addSchedule(ExamSchedule s) { examScheduleRepository.save(s); }
    public void confirmRandomization(Integer id) { examScheduleRepository.findById(id).ifPresent(s -> { s.setRandomizationConfirmed(true); examScheduleRepository.save(s); }); }
}

