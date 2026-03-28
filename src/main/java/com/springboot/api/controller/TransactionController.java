package com.springboot.api.controller;


import com.springboot.api.dto.response.TransactionDto;
import com.springboot.api.entity.users.User;
import com.springboot.api.service.CloudinaryService;
import com.springboot.api.service.ExcelService;
import com.springboot.api.service.TransactionService;
import com.springboot.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.time.LocalDateTime;

@RequestMapping("/api/transactions")
@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final UserService userService;
    private final ExcelService excelService;
    private final CloudinaryService cloudinaryService;
    private final TransactionService transactionService;

    // CREATE TRANSACTION
    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(
            @Valid @RequestBody TransactionDto dto,
            Principal principal) {
        String name = principal.getName();
        TransactionDto saved = transactionService.saveTransaction(dto, name);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PAGINATION (LOADS TRANSACTIONS PAGE BY PAGE)
    @GetMapping
    public ResponseEntity<?> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                transactionService.getTransactions(email, page, size)
        );
    }

    //UPDATE TRANSACTION
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(
            @PathVariable Long id,
            @RequestBody TransactionDto dto,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                transactionService.updateTransaction(id, dto, email)
        );
    }

    // DELETE TRANSACTION
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        transactionService.deleteTransaction(id, email);
        return ResponseEntity.ok("Transaction deleted successfully");
    }

    // RECENT TRANSACTIONS
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentTransactions(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                transactionService.getRecentTransactions(email)
        );
    }

    // FINANCE SUMMARY
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                transactionService.getSummary(email)
        );
    }

    // MONTHLY CHART
    @GetMapping("/chart/monthly-expense")
    public ResponseEntity<?> getMonthlyExpenseChart(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                transactionService.getMonthlyExpenseChart(email)
        );
    }

    // FILTER TRANSACTIONS BY DATE
    @GetMapping("/filter")
    public ResponseEntity<?> filterTransactions(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                transactionService.getTransactionsByDateRange(email, start, end)
        );
    }

    // EXPORT EXCEL REPORT
    @GetMapping("/export")
    public ResponseEntity<?> exportExcel(Authentication authentication) throws Exception {
        String email = authentication.getName();
        ByteArrayInputStream stream =
                excelService.exportToExcel(email);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=transactions.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(stream));
    }

    // SEND EMAIL REPORT
    @PostMapping("/send-report")
    public ResponseEntity<?> sendReport(Authentication authentication) throws Exception {
        String email = authentication.getName();
        transactionService.sendTransactionReport(email);
        return ResponseEntity.ok("Report sent to email successfully");
    }

    // UPLOAD PROFILE IMAGE
    @PostMapping("/upload-profile")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws Exception {

        String email = authentication.getName();
        String imageUrl = cloudinaryService.uploadImage(file);
        User user = userService.findByEmail(email);
        user.setProfileImageUrl(imageUrl);
        userService.save(user);
        return ResponseEntity.ok(imageUrl);
    }

    //FINANCIAL ANALYTICS
    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                transactionService.getAnalytics(email)
        );
    }

    //EXPENSE PREDICTION
    @GetMapping("/prediction")
    public ResponseEntity<?> predictExpense(Authentication auth) {
        return ResponseEntity.ok(
                transactionService.predictNextMonthExpense(auth.getName())
        );
    }

    //SUSPICIOUS TRANSACTION DETECTION
    @GetMapping("/suspicious")
    public ResponseEntity<?> suspicious(Authentication auth) {
        return ResponseEntity.ok(
                transactionService.getSuspiciousTransactions(auth.getName())
        );
    }
}