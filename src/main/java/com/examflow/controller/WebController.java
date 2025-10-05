package com.examflow.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.examflow.model.ExamHall;
import com.examflow.model.ExamSchedule;
import com.examflow.model.StudRand;
import com.examflow.model.Student;
import com.examflow.service.SeatingService;

@Controller
public class WebController {

    @Autowired
    private SeatingService seatingService;

    @GetMapping("/")
    public String index() { return "index"; }

    @GetMapping("/student")
    public String studentPortal() { return "studentView"; }

    @PostMapping("/student/hall")
    public String showStudentHall(@RequestParam String registerNo, Model model) {
        Optional<StudRand> arrangement = seatingService.findArrangementByRegisterNo(registerNo);
        Optional<ExamSchedule> scheduleOpt = seatingService.findNextExam(); // Corrected call

        if (scheduleOpt.isPresent()) {
            ExamSchedule schedule = scheduleOpt.get();
            if (LocalDateTime.now().isAfter(schedule.getSlotStartTime().minusHours(2))) {
                if (arrangement.isPresent()) {
                    model.addAttribute("arrangement", arrangement.get());
                } else {
                    model.addAttribute("message", "Register number not found.");
                }
            } else {
                model.addAttribute("message", "Arrangement revealed 2 hours before exam.");
            }
        } else {
            model.addAttribute("message", "No upcoming exam found.");
        }
        return "studentView";
    }

    // ... (rest of the controller remains the same)
    @GetMapping("/invigilator")
    public String invigilatorPortal(Model model) {
        model.addAttribute("halls", seatingService.getAllExamHalls());
        return "invigilatorView";
    }

    @PostMapping("/invigilator/hall")
    public String showInvigilatorHall(@RequestParam String examhallNo, Model model) {
        model.addAttribute("arrangements", seatingService.findArrangementsByHallNo(examhallNo));
        model.addAttribute("selectedHall", examhallNo);
        model.addAttribute("halls", seatingService.getAllExamHalls());
        return "invigilatorView";
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("students", seatingService.getAllStudents());
        model.addAttribute("halls", seatingService.getAllExamHalls());
        model.addAttribute("schedules", seatingService.getAllSchedules());
        model.addAttribute("newStudent", new Student());
        model.addAttribute("newHall", new ExamHall());
        model.addAttribute("newSchedule", new ExamSchedule());
        return "adminView";
    }

    @PostMapping("/admin/student/add")
    public String addStudent(@ModelAttribute Student newStudent, RedirectAttributes redirectAttributes) {
        seatingService.addStudent(newStudent);
        redirectAttributes.addFlashAttribute("message", "Student added!");
        return "redirect:/admin";
    }

    @PostMapping("/admin/student/delete")
    public String deleteStudent(@RequestParam String registerNo, RedirectAttributes redirectAttributes) {
        seatingService.deleteStudent(registerNo);
        redirectAttributes.addFlashAttribute("message", "Student deleted!");
        return "redirect:/admin";
    }

    @PostMapping("/admin/hall/add")
    public String addHall(@ModelAttribute ExamHall newHall, RedirectAttributes redirectAttributes) {
        seatingService.addExamHall(newHall);
        redirectAttributes.addFlashAttribute("message", "Hall added!");
        return "redirect:/admin";
    }
    
    @PostMapping("/admin/hall/delete")
    public String deleteHall(@RequestParam String examhallNo, RedirectAttributes redirectAttributes) {
        seatingService.deleteExamHall(examhallNo);
        redirectAttributes.addFlashAttribute("message", "Hall deleted!");
        return "redirect:/admin";
    }

    @PostMapping("/admin/schedule/add")
    public String addSchedule(@ModelAttribute ExamSchedule newSchedule, RedirectAttributes redirectAttributes) {
        seatingService.addSchedule(newSchedule);
        redirectAttributes.addFlashAttribute("message", "Schedule added!");
        return "redirect:/admin";
    }

    @PostMapping("/admin/schedule/confirm")
    public String confirmSchedule(@RequestParam Long scheduleId, RedirectAttributes redirectAttributes) {
        seatingService.confirmSchedule(scheduleId);
        redirectAttributes.addFlashAttribute("message", "Schedule confirmed!");
        return "redirect:/admin";
    }
}

