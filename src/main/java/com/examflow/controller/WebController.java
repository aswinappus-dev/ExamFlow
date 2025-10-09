package com.examflow.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.examflow.model.ExamGroup;
import com.examflow.model.ExamHall;
import com.examflow.model.ExamHallAvailability;
import com.examflow.model.ExamSchedule;
import com.examflow.model.Student;
import com.examflow.service.SeatingService;

import jakarta.servlet.http.HttpSession;

@Controller
public class WebController {

    @Autowired
    private SeatingService seatingService;

    // --- PUBLIC VIEWS ---

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("initialView", "home-view");
        model.addAttribute("hallsByBlock", seatingService.getHallsGroupedByBlock());
        model.addAttribute("allStudents", seatingService.getAllStudents());
        // Pass data readiness flag to hide/show forms on public dashboards
        model.addAttribute("isDataReady", seatingService.isSeatingDataReadyForPublicView());
        return "index";
    }

    @GetMapping("/about")
    public String aboutPage() {
        return "about";
    }

    @GetMapping("/support")
    public String supportPage() {
        return "support";
    }

    // --- STUDENT & INVIGILATOR HANDLERS ---

    @PostMapping("/student")
    public String handleStudentRequest(@RequestParam String registerNo, Model model) {
        model.addAttribute("studentResult", seatingService.getStudentArrangement(registerNo));
        model.addAttribute("hallsByBlock", seatingService.getHallsGroupedByBlock());
        model.addAttribute("allStudents", seatingService.getAllStudents());
        model.addAttribute("isDataReady", seatingService.isSeatingDataReadyForPublicView());
        model.addAttribute("initialView", "student-view");
        return "index";
    }

    @PostMapping("/invigilator")
    public String handleInvigilatorRequest(@RequestParam String examhallNo, Model model) {
        model.addAttribute("invigilatorResult", seatingService.getHallArrangement(examhallNo));
        model.addAttribute("hallsByBlock", seatingService.getHallsGroupedByBlock());
        model.addAttribute("allStudents", seatingService.getAllStudents());
        model.addAttribute("isDataReady", seatingService.isSeatingDataReadyForPublicView());
        model.addAttribute("selectedHall", examhallNo);
        model.addAttribute("initialView", "invigilator-view");
        return "index";
    }

    // --- ADMIN VIEWS & AUTHENTICATION (TESTING BYPASS ACTIVE) ---

    @GetMapping("/admin")
    public String adminPortal(Model model, HttpSession session) {
        // --- SECURITY BYPASS FOR TESTING: Always set isAdmin to true ---
        session.setAttribute("isAdmin", true);
        // --- END BYPASS ---
        
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
    
    // --- NEW API ENDPOINT FOR DYNAMIC UI ---
    @GetMapping("/admin/api/availability/{scheduleId}")
    @ResponseBody // This is crucial to return JSON, not a template name
    public List<ExamHallAvailability> getAvailabilitiesForSchedule(@PathVariable Integer scheduleId) {
        return seatingService.getHallAvailabilitiesForSchedule(scheduleId);
    }
    
    @PostMapping("/admin/login")
    public String handleAdminLogin(@RequestParam String email, @RequestParam String code, HttpSession session, RedirectAttributes redirectAttributes) {
        // --- SECURITY BYPASS FOR TESTING: Always grant access ---
        session.setAttribute("isAdmin", true);
        // --- END BYPASS ---
        
        // Original logic kept for structure, but bypass above overrides it
        if (!seatingService.verifyAdminCredentials(session, email, code) && !((Boolean)session.getAttribute("isAdmin"))) {
            redirectAttributes.addFlashAttribute("adminLoginError", "Invalid email or verification code.");
        }
        return "redirect:/admin";
    }
    
    @PostMapping("/admin/logout")
    public String adminLogout(HttpSession session) { 
        session.invalidate(); 
        return "redirect:/";
    }
    
    // --- ADMIN ACTIONS ---

    @PostMapping("/admin/confirmRandomization")
    public String confirmRandomization(@RequestParam Integer scheduleId, RedirectAttributes redirectAttributes) {
        seatingService.confirmRandomization(scheduleId);
        redirectAttributes.addFlashAttribute("confirmationSuccess", true);
        return "redirect:/admin";
    }

    @PostMapping("/admin/addSchedule")
    public String addSchedule() {
        // --- FAST TESTING FIX: Create a default schedule 5 mins from now ---
        LocalDateTime now = LocalDateTime.now();
        ExamSchedule schedule = new ExamSchedule();
        schedule.setSlotStartTime(now.plusMinutes(5));
        schedule.setSlotEndTime(now.plusHours(2).plusMinutes(5)); // 2 hours later
        seatingService.addSchedule(schedule);
        return "redirect:/admin";
    }

    @PostMapping("/admin/addGroupToSchedule")
    public String addGroupToSchedule(@RequestParam Integer scheduleId, @RequestParam String studentYear, @RequestParam String studentBranch) {
        ExamGroup group = new ExamGroup();
        group.setScheduleId(scheduleId);
        group.setStudentYear(studentYear);
        group.setStudentBranch(studentBranch);
        seatingService.addExamGroup(group);
        return "redirect:/admin";
    }

    @PostMapping("/admin/updateHallAvailability")
    public String updateHallAvailability(@RequestParam("scheduleId") Integer scheduleId,
                                         @RequestParam(name = "availableHalls", required = false) List<String> availableHallNumbers,
                                         RedirectAttributes redirectAttributes) {
        seatingService.updateHallAvailability(scheduleId, availableHallNumbers);
        redirectAttributes.addFlashAttribute("availabilitySuccess", true);
        return "redirect:/admin";
    }

    // --- STANDARD CRUD ACTIONS ---

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
}

