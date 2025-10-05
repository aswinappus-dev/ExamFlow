package com.examflow.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "exam_schedule")
public class ExamSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String examName;

    private LocalDateTime slotStartTime; 
    private LocalDateTime slotEndTime;
    private boolean randomizationConfirmed = false;

    // --- GETTERS AND SETTERS ---
    
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getExamName() {
        return examName;
    }
    public void setExamName(String examName) {
        this.examName = examName;
    }
    public LocalDateTime getSlotStartTime() {
        return slotStartTime;
    }
    public void setSlotStartTime(LocalDateTime slotStartTime) {
        this.slotStartTime = slotStartTime;
    }
    public LocalDateTime getSlotEndTime() {
        return slotEndTime;
    }
    public void setSlotEndTime(LocalDateTime slotEndTime) {
        this.slotEndTime = slotEndTime;
    }
    public boolean isRandomizationConfirmed() {
        return randomizationConfirmed;
    }
    public void setRandomizationConfirmed(boolean randomizationConfirmed) {
        this.randomizationConfirmed = randomizationConfirmed;
    }
}

