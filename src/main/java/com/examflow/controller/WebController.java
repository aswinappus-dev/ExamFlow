package com.examflow.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.examflow.model.ExamGroup;
import com.examflow.model.ExamHall;
import com.examflow.model.ExamSchedule;
import com.examflow.model.Student;
import com.examflow.service.SeatingService;

import jakarta.servlet.http.HttpSession;

@Controller
public class WebController {

    @Autowired
    private SeatingService seatingService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("initialView", "home-view");
        model.addAttribute("hallsByBlock", seatingService.getHallsGroupedByBlock());
        model.addAttribute("allStudents", seatingService.getAllStudents());
        return "index";
    }

    @PostMapping("/student")
    public String handleStudentRequest(@RequestParam String registerNo, Model model) {
        model.addAttribute("studentResult", seatingService.getStudentArrangement(registerNo));
        model.addAttribute("hallsByBlock", seatingService.getHallsGroupedByBlock());
        model.addAttribute("allStudents", seatingService.getAllStudents());
        model.addAttribute("initialView", "student-view");
        return "index";
    }

    @PostMapping("/invigilator")
    public String handleInvigilatorRequest(@RequestParam String examhallNo, Model model) {
        model.addAttribute("invigilatorResult", seatingService.getHallArrangement(examhallNo));
        model.addAttribute("hallsByBlock", seatingService.getHallsGroupedByBlock());
        model.addAttribute("allStudents", seatingService.getAllStudents());
        model.addAttribute("selectedHall", examhallNo);
        model.addAttribute("initialView", "invigilator-view");
        return "index";
    }

    @GetMapping("/admin")
    public String adminPortal(Model model, HttpSession session) {
        if (session.getAttribute("isAdmin") != null && (Boolean) session.getAttribute("isAdmin")) {
            model.addAttribute("allStudents", seatingService.getAllStudents());
            model.addAttribute("hallsByBlock", seatingService.getHallsGroupedByBlock());
            model.addAttribute("allSchedules", seatingService.getAllSchedules());
            model.addAttribute("unconfirmedExams", seatingService.findAllUnconfirmedExams());
            model.addAttribute("nextConfirmedExam", seatingService.findNextConfirmedExam().orElse(null));
            return "admin_dashboard";
        } else {
            model.addAttribute("hallsByBlock", seatingService.getHallsGroupedByBlock());
            model.addAttribute("allStudents", seatingService.getAllStudents());
            model.addAttribute("initialView", "admin-login-view");
            return "index";
        }
    }

    @PostMapping("/admin/login")
    public String handleAdminLogin(@RequestParam String email, @RequestParam String code, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!seatingService.verifyAdminCredentials(session, email, code)) {
            redirectAttributes.addFlashAttribute("adminLoginError", "Invalid email or verification code.");
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @PostMapping("/admin/confirmRandomization")
    public String confirmRandomization(@RequestParam Integer scheduleId, RedirectAttributes redirectAttributes) {
        seatingService.confirmRandomization(scheduleId);
        redirectAttributes.addFlashAttribute("confirmationSuccess", true);
        return "redirect:/admin";
    }

    @PostMapping("/admin/addGroupToSchedule")
    public String addGroupToSchedule(ExamGroup examGroup, RedirectAttributes redirectAttributes) {
        seatingService.addExamGroup(examGroup);
        redirectAttributes.addFlashAttribute("groupAddedSuccess", "Successfully added group to schedule.");
        return "redirect:/admin";
    }
    
    @PostMapping("/admin/updateHallAvailability")
    public String updateHallAvailability(@RequestParam("scheduleId") Integer scheduleId,
                                         @RequestParam(name = "availableHalls", required = false) List<String> availableHallNumbers,
                                         RedirectAttributes redirectAttributes) {
        seatingService.updateHallAvailability(scheduleId, availableHallNumbers);
        redirectAttributes.addFlashAttribute("availabilitySuccess", "Hall availability has been updated for the selected schedule.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/addStudent")
    public String addStudent(@RequestParam String registerNo) {
        Student s = new Student(); s.setRegisterNo(registerNo); seatingService.addStudent(s);
        return "redirect:/admin";
    }

    @PostMapping("/admin/deleteStudent")
    public String deleteStudent(@RequestParam String registerNo) {
        seatingService.deleteStudent(registerNo);
        return "redirect:/admin";
    }

    @PostMapping("/admin/addHall")
    public String addHall(ExamHall hall) {
        seatingService.addHall(hall);
        return "redirect:/admin";
    }

    @PostMapping("/admin/deleteHall")
    public String deleteHall(@RequestParam String examhallNo) {
        seatingService.deleteHall(examhallNo);
        return "redirect:/admin";
    }

    @PostMapping("/admin/addSchedule")
    public String addSchedule(ExamSchedule schedule) {
        seatingService.addSchedule(schedule);
        return "redirect:/admin";
    }
}

