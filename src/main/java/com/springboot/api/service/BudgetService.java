package com.springboot.api.service;

import com.springboot.api.dto.response.BudgetDto;
import com.springboot.api.entity.budgets.Budget;
import com.springboot.api.entity.categories.Category;
import com.springboot.api.entity.users.User;
import com.springboot.api.repository.BudgetRepository;
import com.springboot.api.repository.CategoryRepository;
import com.springboot.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final UserService userService;
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    // CREATE BUDGET
    public Budget createBudget(BudgetDto dto, String email) {
        User user = userService.findByEmail(email);
        Category category =
                categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));

        Budget budget = new Budget();
        budget.setLimitAmount(dto.getLimitAmount());
        budget.setMonth(dto.getMonth());
        budget.setYear(dto.getYear());
        budget.setCategory(category);
        budget.setUser(user);
        return budgetRepository.save(budget);
    }

    // GET ALL USER BUDGETS
    public List<Budget> getBudgets(String email) {
        User user = userService.findByEmail(email);
        return budgetRepository.findByUser_UserId(user.getUserId());
    }

    // CHECK BUDGET STATUS
    public List<Map<String, Object>> checkBudgets(String email) {
        User user = userService.findByEmail(email);
        List<Budget> budgets =
                budgetRepository.findByUser_UserId(user.getUserId());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Budget budget : budgets) {
            Double totalExpense =
                    transactionRepository.sumCategoryExpenseForMonth(
                            user.getUserId(),
                            budget.getCategory().getId(),
                            budget.getMonth(),
                            budget.getYear()
                    );
            if (totalExpense == null) totalExpense = 0.0;

            Map<String, Object> map = new HashMap<>();
            map.put("id", budget.getId());
            map.put("categoryId", budget.getCategory().getId());
            map.put("category", budget.getCategory().getName());
            map.put("budget", budget.getLimitAmount());
            map.put("month", budget.getMonth());
            map.put("year", budget.getYear());
            map.put("spent", totalExpense);
            map.put("remaining", budget.getLimitAmount() - totalExpense);
            result.add(map);
        }
        return result;
    }

    // UPDATE BUDGET
    public Budget updateBudget(Long id, BudgetDto dto, String email) {
        User user = userService.findByEmail(email);
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        if (!budget.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Unauthorized");
        }
        budget.setLimitAmount(dto.getLimitAmount());
        return budgetRepository.save(budget);
    }

    // DELETE BUDGET
    public void deleteBudget(Long id, String email) {
        User user = userService.findByEmail(email);
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        if (!budget.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Unauthorized");
        }
        budgetRepository.delete(budget);
    }
}