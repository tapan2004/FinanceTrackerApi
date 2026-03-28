package com.springboot.api.scheduler;

import com.springboot.api.entity.users.User;
import com.springboot.api.repository.UserRepository;
import com.springboot.api.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyReminderSchedulerService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 9 * * ?") // every day at 9:00 AM
    public void sendDailyReminder() {
        log.info("Running Daily Reminder Scheduler");

        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.isActive()) {
                emailService.sendEmail(
                        user.getEmail(),
                        "Daily Finance Reminder",
                        "Don't forget to track your income & expenses today 💰"
                );
            }
        }
        log.info("Daily Reminder Completed");
    }
}