package com.examflow.service;

import com.examflow.model.ExamHall;
import com.examflow.model.ExamSchedule;
import com.examflow.model.StudRand;
import com.examflow.model.Student;
import com.examflow.repository.ExamHallRepository;
import com.examflow.repository.ExamScheduleRepository;
import com.examflow.repository.StudRandRepository;
import com.examflow.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatingService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ExamHallRepository examHallRepository;

    @Autowired
    private StudRandRepository studRandRepository;

    @Autowired
    private ExamScheduleRepository examScheduleRepository;

    // --- Core Randomization Logic ---
    @Transactional
    public void performRandomization() {
        studRandRepository.deleteAll();

        List<Student> allStudents = studentRepository.findAll();
        List<ExamHall> allHalls = examHallRepository.findAll();
        Collections.shuffle(allStudents);

        Map<String, List<Student>> studentsByBranch = allStudents.stream()
                .collect(Collectors.groupingBy(student -> student.getRegisterNo().substring(5, 7)));

        int hallIndex = 0;
        for (ExamHall hall : allHalls) {
            if (hallIndex >= allHalls.size()) break;

            int capacity = hall.getCapacity();
            List<Student> assignedStudents = new ArrayList<>();
            List<String> branchesInHall = new ArrayList<>();

            // Logic to fill hall with blocks of students
            // This is a simplified example; real-world logic might be more complex
            for (Map.Entry<String, List<Student>> entry : studentsByBranch.entrySet()) {
                if (assignedStudents.size() >= capacity) break;

                String branch = entry.getKey();
                List<Student> branchStudents = entry.getValue();

                if (!branchStudents.isEmpty() && !branchesInHall.contains(branch)) {
                    // Example: take a block of 15
                    int blockSize = Math.min(15, branchStudents.size());
                    blockSize = Math.min(blockSize, capacity - assignedStudents.size());
                    
                    if (blockSize > 0) {
                        List<Student> block = new ArrayList<>(branchStudents.subList(0, blockSize));
                        assignedStudents.addAll(block);
                        branchStudents.subList(0, blockSize).clear(); // Remove assigned students
                        branchesInHall.add(branch);
                    }
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

    // --- Core Cleanup Logic ---
    @Transactional
    public void clearArrangementsForPastExams() {
        List<ExamSchedule> schedules = examScheduleRepository.findAll();
        for (ExamSchedule schedule : schedules) {
            if (schedule.getSlotEndTime().plusHours(4).isBefore(LocalDateTime.now())) {
                studRandRepository.deleteAll();
                // Optionally delete the old schedule entry
                // examScheduleRepository.delete(schedule);
                break; 
            }
        }
    }
    
    // --- Methods for Controller and Scheduler ---

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public void addStudent(Student student) {
        studentRepository.save(student);
    }

    public void deleteStudent(String registerNo) {
        studentRepository.deleteById(registerNo);
    }

    public List<ExamHall> getAllExamHalls() {
        return examHallRepository.findAll();
    }

    public void addExamHall(ExamHall examHall) {
        examHallRepository.save(examHall);
    }

    public void deleteExamHall(String examhallNo) {
        examHallRepository.deleteById(examhallNo);
    }

    public List<ExamSchedule> getAllSchedules() {
        return examScheduleRepository.findAll();
    }

    public void addSchedule(ExamSchedule schedule) {
        examScheduleRepository.save(schedule);
    }
    
    public void confirmSchedule(Long scheduleId) {
        examScheduleRepository.findById(scheduleId).ifPresent(schedule -> {
            schedule.setRandomizationConfirmed(true);
            examScheduleRepository.save(schedule);
        });
    }

    public Optional<ExamSchedule> findNextExam() {
        return examScheduleRepository.findFirstByRandomizationConfirmedIsTrueAndSlotStartTimeIsAfterOrderBySlotStartTimeAsc(LocalDateTime.now());
    }

    public Optional<StudRand> findArrangementByRegisterNo(String registerNo) {
        return studRandRepository.findByRegisterNo(registerNo);
    }

    public List<StudRand> findArrangementsByHallNo(String examhallNo) {
        return studRandRepository.findByExamhallNo(examhallNo);
    }
}

