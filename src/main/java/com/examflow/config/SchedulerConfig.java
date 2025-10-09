package com.examflow.config;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.examflow.model.ExamSchedule;
import com.examflow.service.SeatingService;

@Component
@EnableScheduling
public class SchedulerConfig {

    @Autowired
    private SeatingService seatingService;

    @Scheduled(cron = "0 * * * * ?") // Runs every minute
    public void scheduleRandomization() {
        // The scheduler should look for CONFIRMED exams to randomize.
        Optional<ExamSchedule> nextExamOpt = seatingService.findNextConfirmedExam();

        if (nextExamOpt.isPresent()) {
            ExamSchedule nextExam = nextExamOpt.get();
            LocalDateTime now = LocalDateTime.now();
            long minutesUntilExam = ChronoUnit.MINUTES.between(now, nextExam.getSlotStartTime());
            
        if (minutesUntilExam == 1) { 
            System.out.println("SCHEDULER: Triggering randomization 1 minute before exam at: " + nextExam.getSlotStartTime());
            seatingService.performRandomization();
            }
        }
    }

    @Scheduled(cron = "0 * * * * ?") // Runs every minute for testing
    public void scheduleCleanup() {
        seatingService.clearArrangementsForPastExams();
    }
}

