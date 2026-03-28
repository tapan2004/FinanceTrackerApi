package com.springboot.api.scheduler;

import com.springboot.api.entity.users.User;
import com.springboot.api.repository.UserRepository;
import com.springboot.api.service.EmailService;
import com.springboot.api.service.ExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyReminderSchedulerService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ExcelService excelService;

    @Scheduled(cron = "0 0 10 1 * ?") // 10 AM on 1st of every month
    public void sendMonthlyReports() {

        log.info("Starting monthly finance report scheduler...");

        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (!user.isActive()) continue;
            try {
                ByteArrayInputStream excel =
                        excelService.exportToExcel(user.getEmail());

                emailService.sendEmailWithAttachment(
                        user.getEmail(),
                        "Your Monthly Finance Report",
                        "Attached is your monthly finance report.",
                        excel,
                        "monthly-report.xlsx"
                );
                log.info("Report sent to {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send report to {}",
                        user.getEmail(), e);
            }
        }
        log.info("Monthly report scheduler completed.");
    }
}