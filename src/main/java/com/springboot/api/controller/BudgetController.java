package com.springboot.api.controller;

import com.springboot.api.dto.response.BudgetDto;
import com.springboot.api.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/budgets")
@RestController
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    // CREATE BUDGET
    @PostMapping
    public ResponseEntity<?> createBudget(
            @RequestBody BudgetDto dto,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                budgetService.createBudget(dto, email)
        );
    }

    // GET USER BUDGETS
    @GetMapping
    public ResponseEntity<?> getBudgets(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                budgetService.getBudgets(email)
        );
    }

    // CHECK BUDGET STATUS
    @GetMapping("/check")
    public ResponseEntity<?> checkBudget(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                budgetService.checkBudgets(email)
        );
    }
}