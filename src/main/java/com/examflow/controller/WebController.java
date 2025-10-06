package com.examflow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        model.addAttribute("allHalls", seatingService.getAllHalls());
        return "index";
    }

    @PostMapping("/student")
    public String handleStudentRequest(@RequestParam String registerNo, Model model) {
        model.addAttribute("studentResult", seatingService.getStudentArrangement(registerNo));
        model.addAttribute("initialView", "student-view");
        return "index";
    }

    @PostMapping("/invigilator")
    public String handleInvigilatorRequest(@RequestParam String examhallNo, Model model) {
        model.addAttribute("invigilatorResult", seatingService.getHallArrangement(examhallNo));
        model.addAttribute("allHalls", seatingService.getAllHalls());
        model.addAttribute("selectedHall", examhallNo);
        model.addAttribute("initialView", "invigilator-view");
        return "index";
    }

    @GetMapping("/admin")
    public String adminPortal(Model model, HttpSession session) {
        if (session.getAttribute("isAdmin") != null && (Boolean) session.getAttribute("isAdmin")) {
            model.addAttribute("allStudents", seatingService.getAllStudents());
            model.addAttribute("allHalls", seatingService.getAllHalls());
            model.addAttribute("allSchedules", seatingService.getAllSchedules());
            model.addAttribute("nextExam", seatingService.findNextExam().orElse(null));
            return "admin_dashboard";
        } else {
            model.addAttribute("initialView", "admin-login-view");
            return "index";
        }
    }
    
    @PostMapping("/admin/send-code")
    public String sendAdminCode(@RequestParam String email, HttpSession session, RedirectAttributes redirectAttributes) {
        boolean wasSent = seatingService.generateAndStoreVerificationCode(session, email);
        if (wasSent) {
            redirectAttributes.addFlashAttribute("showCodeForm", true);
        } else {
            redirectAttributes.addFlashAttribute("adminLoginError", "This email is not registered as an administrator.");
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/verify-code")
    public String verifyAdminCode(@RequestParam String code, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!seatingService.verifyAdminCode(session, code)) {
            redirectAttributes.addFlashAttribute("adminLoginError", "Invalid or expired code.");
            redirectAttributes.addFlashAttribute("showCodeForm", true);
        }
        return "redirect:/admin";
    }
    
    @PostMapping("/admin/logout")
    public String adminLogout(HttpSession session) { session.invalidate(); return "redirect:/"; }
    
    @PostMapping("/admin/addStudent")
    public String addStudent(@RequestParam String registerNo) {
        Student s = new Student(); s.setRegisterNo(registerNo); seatingService.addStudent(s);
        return "redirect:/admin";
    }
    @PostMapping("/admin/deleteStudent")
    public String deleteStudent(@RequestParam String registerNo) { seatingService.deleteStudent(registerNo); return "redirect:/admin"; }
    @PostMapping("/admin/addHall")
    public String addHall(ExamHall h) { seatingService.addHall(h); return "redirect:/admin"; }
    @PostMapping("/admin/deleteHall")
    public String deleteHall(@RequestParam String examhallNo) { seatingService.deleteHall(examhallNo); return "redirect:/admin"; }
    @PostMapping("/admin/addSchedule")
    public String addSchedule(ExamSchedule s) { seatingService.addSchedule(s); return "redirect:/admin"; }
    @PostMapping("/admin/confirmRandomization")
    public String confirmRandomization(@RequestParam Integer scheduleId, RedirectAttributes ra) {
        seatingService.confirmRandomization(scheduleId);
        ra.addFlashAttribute("confirmationSuccess", true);
        return "redirect:/admin";
    }
}

