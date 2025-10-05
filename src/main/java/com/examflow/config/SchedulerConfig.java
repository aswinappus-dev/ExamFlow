package com.examflow.config;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.examflow.model.ExamSchedule;
import com.examflow.service.SeatingService;

@Component
public class SchedulerConfig {

    private static final Logger log = LoggerFactory.getLogger(SchedulerConfig.class);

    @Autowired
    private SeatingService seatingService;

    // This task runs every minute to check if it's time to randomize seats.
    @Scheduled(cron = "0 * * * * *")
    public void scheduleRandomization() {
        Optional<ExamSchedule> scheduleOpt = seatingService.findNextExam();
        
        if (scheduleOpt.isPresent()) {
            ExamSchedule schedule = scheduleOpt.get();
            LocalDateTime randomizationTime = schedule.getSlotStartTime().minusHours(2);

            // Check if the current time is within the same minute as the target randomization time
            if (LocalDateTime.now().getMinute() == randomizationTime.getMinute() &&
                LocalDateTime.now().getHour() == randomizationTime.getHour() &&
                LocalDateTime.now().toLocalDate().isEqual(randomizationTime.toLocalDate())) {

                log.info("Triggering randomization for exam: {}", schedule.getExamName());
                seatingService.performRandomization();
            }
        }
    }

    // This task runs every hour to clean up old data.
    @Scheduled(cron = "0 0 * * * *")
    public void scheduleCleanup() {
        log.info("Checking for past exam data to clean up...");
        seatingService.clearArrangementsForPastExams();
    }
}

