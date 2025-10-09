package com.examflow.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examflow.model.Admin;
import com.examflow.model.ExamGroup;
import com.examflow.model.ExamHall;
import com.examflow.model.ExamHallAvailability;
import com.examflow.model.ExamSchedule;
import com.examflow.model.StudRand;
import com.examflow.model.Student;
import com.examflow.repository.AdminRepository;
import com.examflow.repository.ExamGroupRepository;
import com.examflow.repository.ExamHallAvailabilityRepository;
import com.examflow.repository.ExamHallRepository;
import com.examflow.repository.ExamScheduleRepository;
import com.examflow.repository.StudRandRepository;
import com.examflow.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class SeatingService {

    @Autowired private StudentRepository studentRepository;
    @Autowired private ExamHallRepository examHallRepository;
    @Autowired private StudRandRepository studRandRepository;
    @Autowired private ExamScheduleRepository examScheduleRepository;
    @Autowired private AdminRepository adminRepository;
    @Autowired private ExamGroupRepository examGroupRepository;
    @Autowired private ExamHallAvailabilityRepository availabilityRepository;

    @Transactional
    public void performRandomization() {
        studRandRepository.deleteAll();

        // Step 1: Find the confirmed exam and its assigned student groups
        Optional<ExamSchedule> nextExamOpt = findNextConfirmedExam();
        if (nextExamOpt.isEmpty()) {
            System.err.println("RANDOMIZATION HALTED: No confirmed exam found.");
            return;
        }
        Integer scheduleId = nextExamOpt.get().getId();
        List<ExamGroup> requiredGroups = examGroupRepository.findByScheduleId(scheduleId);
        if (requiredGroups.isEmpty()) {
            System.err.println("RANDOMIZATION HALTED: No student groups assigned to exam schedule ID: " + scheduleId);
            return;
        }

        // Step 2: Fetch and prepare ONLY the eligible students
        Map<String, List<Student>> studentsByBranch = new HashMap<>();
        for (ExamGroup group : requiredGroups) {
            // FIXED: Use correct getter names getStudentYear() and getStudentBranch()
            String prefix = "CEC" + group.getStudentYear() + group.getStudentBranch();
            List<Student> students = studentRepository.findByRegisterNoStartingWith(prefix);
            Collections.shuffle(students); // Shuffle students within their own branch for fairness
            // FIXED: Use correct getter name getStudentBranch()
            studentsByBranch.put(group.getStudentBranch(), students);
        }
        int totalStudents = studentsByBranch.values().stream().mapToInt(List::size).sum();
        if (totalStudents == 0) {
            System.err.println("RANDOMIZATION HALTED: No eligible students found for the assigned groups.");
            return;
        }
        System.out.println("Randomizing for " + totalStudents + " eligible students.");

        // Step 3: Get ONLY AVAILABLE halls and allocate necessary capacity
        List<ExamHall> allHalls = examHallRepository.findAll();
        List<ExamHallAvailability> unavailableHalls = availabilityRepository.findByScheduleIdAndIsAvailableFalse(scheduleId);
        Set<String> unavailableHallNumbers = unavailableHalls.stream()
                                                .map(ExamHallAvailability::getExamhallNo)
                                                .collect(Collectors.toSet());
        
        List<ExamHall> availableHalls = allHalls.stream()
                                              .filter(hall -> !unavailableHallNumbers.contains(hall.getExamhallNo()))
                                              .sorted(Comparator.comparing(ExamHall::getExamhallNo))
                                              .collect(Collectors.toList());

        List<ExamHall> allocatedHalls = new ArrayList<>();
        int totalCapacity = 0;
        for (ExamHall hall : availableHalls) {
            allocatedHalls.add(hall);
            totalCapacity += hall.getCapacity();
            if (totalCapacity >= totalStudents) break;
        }
        
        if (totalCapacity < totalStudents) {
             System.err.println("CRITICAL ERROR: Not enough capacity in AVAILABLE halls for all students!");
             return;
        }

        // Step 4: HALL-BLOCK FIRST Seating Strategy
        allocatedHalls.sort(Comparator.comparing(ExamHall::getBlockno).thenComparing(ExamHall::getExamhallNo));

        List<Student> finalStudentList = studentsByBranch.values().stream()
                                               .flatMap(List::stream)
                                               .collect(Collectors.toList());
        
        int studentIdx = 0;
        for (int i = 0; i < allocatedHalls.size(); i++) {
            ExamHall currentHall = allocatedHalls.get(i);
            boolean isLastHall = (i == allocatedHalls.size() - 1);
            int studentsRemaining = totalStudents - studentIdx;
            int studentsToPlaceInHall = Math.min(currentHall.getCapacity(), studentsRemaining);
            List<Student> studentsForThisHall = finalStudentList.subList(studentIdx, studentIdx + studentsToPlaceInHall);

            if (isLastHall && studentsToPlaceInHall > 0 && studentsToPlaceInHall < currentHall.getCapacity()) {
                int studentInHallIdx = 0;
                for (int seatNo = 1; seatNo <= currentHall.getCapacity() && studentInHallIdx < studentsForThisHall.size(); seatNo++) {
                    if (seatNo % 2 != 0) { // Odd seat number (1, 3, 5...)
                        saveArrangement(studentsForThisHall.get(studentInHallIdx), currentHall, seatNo);
                        studentInHallIdx++;
                    }
                }
            } else {
                for (int seat = 0; seat < studentsForThisHall.size(); seat++) {
                    saveArrangement(studentsForThisHall.get(seat), currentHall, seat + 1);
                }
            }
            studentIdx += studentsToPlaceInHall;
        }
        System.out.println("Randomization complete for exam ID " + scheduleId);
    }

    public boolean isSeatingDataReadyForPublicView() {
        Optional<ExamSchedule> nextConfirmedExam = findNextConfirmedExam();
        if (nextConfirmedExam.isEmpty()) {
            return false; // No confirmed exam, so no data
        }
        // Check if the reveal time (1 min before exam) has passed
        LocalDateTime revealTime = nextConfirmedExam.get().getSlotStartTime().minusMinutes(1);
        if (LocalDateTime.now().isBefore(revealTime)) {
            return false; // It's too early, so data is not "available"
        }
        // Finally, check if the stud_rand table has any data
        return studRandRepository.count() > 0;
    }

    private void saveArrangement(Student student, ExamHall hall, int seatNo) {
        StudRand arrangement = new StudRand();
        arrangement.setRegisterNo(student.getRegisterNo());
        arrangement.setExamhallNo(hall.getExamhallNo());
        arrangement.setBlockno(hall.getBlockno());
        arrangement.setSeatNo(seatNo);
        studRandRepository.save(arrangement);
    }

    @Transactional
    public void updateHallAvailability(Integer scheduleId, List<String> availableHallNumbers) {
        List<ExamHall> allHalls = examHallRepository.findAll();
        Set<String> availableSet = (availableHallNumbers != null) ? new HashSet<>(availableHallNumbers) : Collections.emptySet();

        List<ExamHallAvailability> existingSettings = availabilityRepository.findByScheduleId(scheduleId);
        availabilityRepository.deleteAll(existingSettings);
        
        for (ExamHall hall : allHalls) {
            ExamHallAvailability availability = new ExamHallAvailability();
            availability.setScheduleId(scheduleId);
            availability.setExamhallNo(hall.getExamhallNo());
            availability.setAvailable(availableSet.contains(hall.getExamhallNo()));
            availabilityRepository.save(availability);
        }
    }

    @Transactional
    public void clearArrangementsForPastExams() {
        // MODIFIED: Cleanup now runs 5 minutes after an exam ends
        List<ExamSchedule> pastSchedules = examScheduleRepository.findBySlotEndTimeBefore(LocalDateTime.now().minusMinutes(5));
        if (!pastSchedules.isEmpty()) {
            studRandRepository.deleteAll();
            System.out.println("Cleared arrangements for past exams.");
        }
    }

    public Optional<ExamSchedule> findNextConfirmedExam() {
        return examScheduleRepository.findFirstByRandomizationConfirmedIsTrueAndSlotStartTimeAfterOrderBySlotStartTimeAsc(LocalDateTime.now());
    }

    public List<ExamSchedule> findAllUnconfirmedExams() {
        return examScheduleRepository.findAllByRandomizationConfirmedIsFalseAndSlotStartTimeAfterOrderBySlotStartTimeAsc(LocalDateTime.now());
    }

    public Map<String, Object> getStudentArrangement(String registerNo) {
        Map<String, Object> result = new HashMap<>();
        if (studentRepository.findById(registerNo).isEmpty()) {
             result.put("message", "Student data does not exist. Please check the format.");
             return result;
        }
        var nextConfirmedExam = findNextConfirmedExam();
        if (nextConfirmedExam.isEmpty()) {
            result.put("message", "No upcoming exam is scheduled for randomization.");
            return result;
        }
        // MODIFIED: Reveal time is now 30 seconds before the exam starts
        LocalDateTime revealTime = nextConfirmedExam.get().getSlotStartTime().minusSeconds(30);
        if (LocalDateTime.now().isBefore(revealTime)) {
            result.put("revealTime", revealTime.toString());
            return result;
        }
        studRandRepository.findByRegisterNo(registerNo)
            .ifPresentOrElse(
                arrangement -> result.put("arrangement", arrangement),
                () -> result.put("message", "Your register number was not found in the arrangement.")
            );
        return result;
    }

    public Map<String, Object> getHallArrangement(String examhallNo) {
        Map<String, Object> result = new HashMap<>();
        var nextConfirmedExam = findNextConfirmedExam();
        if (nextConfirmedExam.isEmpty()) {
            result.put("message", "No upcoming exam is scheduled for randomization.");
            return result;
        }
        // MODIFIED: Reveal time is now 30 seconds before the exam starts
        LocalDateTime revealTime = nextConfirmedExam.get().getSlotStartTime().minusSeconds(30);
        if (LocalDateTime.now().isBefore(revealTime)) {
            result.put("revealTime", revealTime.toString());
            return result;
        }
        // FIXED: Use the new sorted method
        result.put("arrangements", studRandRepository.findByExamhallNoOrderBySeatNoAsc(examhallNo));
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

    public void addExamGroup(ExamGroup group) {
        examGroupRepository.save(group);
    }

    public Map<String, List<ExamHall>> getHallsGroupedByBlock() {
        return examHallRepository.findAll().stream().collect(Collectors.groupingBy(ExamHall::getBlockno, TreeMap::new, Collectors.toList()));
    }

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


