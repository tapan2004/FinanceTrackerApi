package com.springboot.api.service;


import com.springboot.api.dto.response.SummaryDto;
import com.springboot.api.dto.response.TransactionDto;
import com.springboot.api.entity.budgets.Budget;
import com.springboot.api.entity.categories.Category;
import com.springboot.api.entity.categories.CategoryType;
import com.springboot.api.entity.transactions.Transaction;
import com.springboot.api.entity.transactions.TransactionType;
import com.springboot.api.entity.users.User;
import com.springboot.api.repository.BudgetRepository;
import com.springboot.api.repository.CategoryRepository;
import com.springboot.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final EmailService emailService;
    private final ExcelService excelService;
    private final BudgetRepository budgetRepository;
    private final AIExpenseService aiExpenseService;

    //CREATE A TRANSACTION (INCOME/EXPENSE).
    public TransactionDto saveTransaction(TransactionDto dto, String email) {
        User user = userService.findByEmail(email);

        if (dto.getCategoryId() == null) {
            String predictedCategory =
                    aiExpenseService.predictCategory(dto.getTitle());

            Category predicted =
                    categoryRepository
                            .findByNameAndUser_UserId(predictedCategory, user.getUserId())
                            .orElseGet(() ->{
                                Category newCategory=new Category();
                                newCategory.setName(predictedCategory);
                                newCategory.setType(CategoryType.EXPENSE);
                                newCategory.setUser(user);
                                return categoryRepository.save(newCategory);
                            });
            dto.setCategoryId(predicted.getId());
        }

        Category category = categoryRepository
                .findByIdAndUser_UserId(dto.getCategoryId(), user.getUserId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        boolean suspicious = detectFraud(user, dto.getAmount());
        Transaction transaction = Transaction.builder()
                .title(dto.getTitle())
                .amount(dto.getAmount())
                .type(dto.getType())
                .note(dto.getNote())
                .user(user)
                .category(category)
                .suspicious(suspicious)
                .build();

        Transaction saved = transactionRepository.save(transaction);

        //Budget Alert Check
        if (saved.isSuspicious()) {
            sendFraudAlert(user, saved);
        }

        // BUDGET ALERT
        if (saved.getType() == TransactionType.EXPENSE) {
            checkBudget(saved);
        }
        return toDto(saved);
    }

    //CONVERT TRANSACTION ENTITY → TRANSACTION DTO
    private TransactionDto toDto(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setTitle(transaction.getTitle());
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setNote(transaction.getNote());
        dto.setCategoryId(transaction.getCategory().getId());
        dto.setCategoryName(transaction.getCategory().getName());
        return dto;
    }

    //GENERATE MONTHLY EXPENSE CHART DATA.
    public List<Map<String, Object>> getMonthlyExpenseChart(String email) {
        User user = userService.findByEmail(email);
        List<Object[]> data =
                transactionRepository.getMonthlyExpenses(user.getUserId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : data) {
            Map<String, Object> map = new HashMap<>();
            map.put("month", row[0]);
            map.put("total", row[1]);
            result.add(map);
        }
        return result;
    }

    //RECENT TRANSACTIONS
    public List<TransactionDto> getRecentTransactions(String email) {
        User user = userService.findByEmail(email);
        List<Transaction> transactions =
                transactionRepository
                        .findTop5ByUser_UserIdOrderByCreatedAtDesc(user.getUserId());
        return transactions.stream()
                .map(this::toDto)
                .toList();
    }

    //CALCULATE TOTAL INCOME, EXPENSE, BALANCE.
    public Map<String, Double> getSummary(String email) {
        User user = userService.findByEmail(email);
        SummaryDto dto =
                transactionRepository.getIncomeExpenseSummary(user.getUserId());

        double income = dto.income() != null ? dto.income() : 0;
        double expense = dto.expense() != null ? dto.expense() : 0;
        Map<String, Double> summary = new HashMap<>();
        summary.put("totalIncome", income);
        summary.put("totalExpense", expense);
        summary.put("balance", income - expense);
        return summary;
    }

    //FILTER TRANSACTIONS BETWEEN DATES.
    public List<TransactionDto> getTransactionsByDateRange(
            String email,
            LocalDateTime start,
            LocalDateTime end) {
        User user = userService.findByEmail(email);
        List<Transaction> transactions =
                transactionRepository.findTransactionsByDateRange(
                        user.getUserId(),
                        start,
                        end
                );
        return transactions.stream()
                .map(this::toDto)
                .toList();
    }

    //SEND EXCEL REPORT VIA EMAIL.
    public void sendTransactionReport(String email) throws Exception {
        ByteArrayInputStream excelFile = excelService.exportToExcel(email);
        emailService.sendEmailWithAttachment(
                email,
                "Your Finance Report",
                "Please find attached your transaction report.",
                excelFile,
                "transactions.xlsx"
        );
    }

    //PAGINATION SUPPORT.
    public Page<TransactionDto> getTransactions(String email, int page, int size) {
        User user = userService.findByEmail(email);
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions =
                transactionRepository.findByUser_UserId(user.getUserId(), pageable);
        return transactions.map(this::toDto);
    }

    //UPDATE EXISTING TRANSACTION.
    public TransactionDto updateTransaction(Long id, TransactionDto dto, String email) {
        User user = userService.findByEmail(email);
        Transaction transaction = transactionRepository
                .findByIdAndUser_UserId(id, user.getUserId())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        Category category = categoryRepository
                .findByIdAndUser_UserId(dto.getCategoryId(), user.getUserId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        transaction.setTitle(dto.getTitle());
        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setNote(dto.getNote());
        transaction.setCategory(category);
        Transaction saved = transactionRepository.save(transaction);
        return toDto(saved);
    }

    //DELETE A TRANSACTION.
    public void deleteTransaction(Long id, String email) {
        User user = userService.findByEmail(email);
        Transaction transaction = transactionRepository
                .findByIdAndUser_UserId(id, user.getUserId())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        transactionRepository.delete(transaction);
    }

    //ADVANCED FINANCIAL INSIGHTS.
    public Map<String, Object> getAnalytics(String email) {
        User user = userService.findByEmail(email);
        Long userId = user.getUserId();
        Double income = transactionRepository.getTotalIncome(userId);
        Double expense = transactionRepository.getTotalExpense(userId);
        if (income == null) income = 0.0;
        if (expense == null) expense = 0.0;
        List<Object[]> categoryData =
                transactionRepository.getTopExpenseCategory(userId);
        String topCategory = "N/A";
        if (!categoryData.isEmpty()) {
            topCategory = categoryData.getFirst()[0].toString();
        }
        List<Double> monthlyTotals =
                transactionRepository.getMonthlyExpenseTotals(userId);
        double avgExpense = 0.0;

        if(!monthlyTotals.isEmpty()){
            avgExpense = monthlyTotals
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
        }
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalIncome", income);
        analytics.put("totalExpense", expense);
        analytics.put("balance", income - expense);
        analytics.put("topExpenseCategory", topCategory);
        analytics.put("monthlyAverageExpense", avgExpense);
        return analytics;
    }

    //CHECK IF USER EXCEEDED CATEGORY BUDGET.
    private void checkBudget(Transaction transaction) {
        int month = transaction.getCreatedAt().getMonthValue();
        int year = transaction.getCreatedAt().getYear();

        Optional<Budget> budget =
                budgetRepository.findByUser_UserIdAndCategory_IdAndMonthAndYear(
                        transaction.getUser().getUserId(),
                        transaction.getCategory().getId(),
                        month,
                        year
                );
        if (budget.isEmpty()) return;

        Double totalExpense =
                transactionRepository.sumCategoryExpenseForMonth(
                        transaction.getUser().getUserId(),
                        transaction.getCategory().getId(),
                        month,
                        year
                );

        if (totalExpense == null) totalExpense = 0.0;
        double limit = budget.get().getLimitAmount();
        double percentUsed = (totalExpense / limit) * 100;

        String email = transaction.getUser().getEmail();
        String category = transaction.getCategory().getName();

        // 80% ALERT
        if (percentUsed >= 80 && percentUsed < 90) {
            emailService.sendEmail(
                    email,
                    "Budget Warning (80%)",
                    "You have used " + percentUsed + "% of your "
                            + category + " budget."
            );
        }

        // 90% ALERT
        if (percentUsed >= 90 && percentUsed < 100) {
            emailService.sendEmail(
                    email,
                    "Budget Critical (90%)",
                    "You have used " + percentUsed + "% of your "
                            + category + " budget."
            );
        }

        // BUDGET EXCEEDED
        if (percentUsed >= 100) {
            emailService.sendEmail(
                    email,
                    "Budget Exceeded",
                    "You exceeded your budget for category: " + category
            );
        }
    }

    //PREDICT NEXT MONTH'S SPENDING.
    public Map<String, Object> predictNextMonthExpense(String email) {
        User user = userService.findByEmail(email);
        List<Object[]> data =
                transactionRepository.getMonthlyExpenses(user.getUserId());
        double total = 0;
        int months = 0;
        for (Object[] row : data) {
            total += ((Number) row[1]).doubleValue();
            months++;
        }
        double average = months == 0 ? 0 : total / months;
        Map<String, Object> result = new HashMap<>();
        result.put("predictedNextMonthExpense", average);
        return result;
    }

    //DETECT SUSPICIOUS TRANSACTION.
    private boolean detectFraud(User user, Double amount) {
        Double avgExpense =
                transactionRepository.getAverageExpense(user.getUserId());
        if (avgExpense == null) return false;
        return amount > avgExpense * 3;
    }

    //SEND EMAIL ALERT FOR SUSPICIOUS TRANSACTION.
    private void sendFraudAlert(User user, Transaction transaction) {
        String message =
                " Suspicious transaction detected\n\n"
                        + "Title: " + transaction.getTitle() + "\n"
                        + "Amount: " + transaction.getAmount() + "\n"
                        + "Date: " + transaction.getCreatedAt();

        emailService.sendEmail(
                user.getEmail(),
                "Suspicious Transaction Alert",
                message
        );
    }

    //Fetch all suspicious transactions.
    public List<TransactionDto> getSuspiciousTransactions(String email) {
        User user = userService.findByEmail(email);
        return transactionRepository
                .findByUser_UserIdAndSuspiciousTrue(user.getUserId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    // DAILY SPENDING DATA FOR HEATMAP
    public List<Map<String, Object>> getDailySpending(String email) {
        User user = userService.findByEmail(email);
        List<Transaction> transactions = transactionRepository.findByUser_UserId(user.getUserId());
        
        Map<String, Double> dailyMap = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE && t.getCreatedAt() != null) {
                String date = t.getCreatedAt().toLocalDate().toString();
                dailyMap.merge(date, t.getAmount(), Double::sum);
            }
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Double> entry : dailyMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", entry.getKey());
            map.put("amount", entry.getValue());
            result.add(map);
        }
        return result;
    }

    // FINANCIAL HEALTH SCORE (0-850)
    public Map<String, Object> getFinancialHealthScore(String email) {
        User user = userService.findByEmail(email);
        Long userId = user.getUserId();

        Double income = transactionRepository.getTotalIncome(userId);
        Double expense = transactionRepository.getTotalExpense(userId);
        if (income == null) income = 0.0;
        if (expense == null) expense = 0.0;

        // 1. Savings Rate Score (0-250): How much you save vs earn
        double savingsRate = income > 0 ? ((income - expense) / income) * 100 : 0;
        double savingsScore = Math.min(250, Math.max(0, savingsRate * 5));

        // 2. Budget Adherence Score (0-250): Are you within budgets?
        List<Budget> budgets = budgetRepository.findByUser_UserId(userId);
        double budgetScore = 250; // Full marks if no budgets set
        if (!budgets.isEmpty()) {
            int withinBudget = 0;
            for (Budget b : budgets) {
                Double spent = transactionRepository.sumCategoryExpenseForMonth(
                        userId, b.getCategory().getId(), b.getMonth(), b.getYear());
                if (spent == null) spent = 0.0;
                if (spent <= b.getLimitAmount()) withinBudget++;
            }
            budgetScore = ((double) withinBudget / budgets.size()) * 250;
        }

        // 3. Spending Consistency (0-200): Low variance = good
        List<Double> monthlyTotals = transactionRepository.getMonthlyExpenseTotals(userId);
        double consistencyScore = 200;
        if (monthlyTotals.size() >= 2) {
            double avg = monthlyTotals.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double variance = monthlyTotals.stream().mapToDouble(v -> Math.pow(v - avg, 2)).average().orElse(0);
            double stdDev = Math.sqrt(variance);
            double cv = avg > 0 ? (stdDev / avg) * 100 : 0;
            consistencyScore = Math.max(0, 200 - cv * 2);
        }

        // 4. Activity Score (0-150): Regular tracking
        List<Transaction> all = transactionRepository.findByUser_UserId(userId);
        double activityScore = Math.min(150, all.size() * 5.0);

        double totalScore = Math.min(850, savingsScore + budgetScore + consistencyScore + activityScore);

        String grade;
        if (totalScore >= 750) grade = "Excellent";
        else if (totalScore >= 600) grade = "Good";
        else if (totalScore >= 400) grade = "Fair";
        else if (totalScore >= 200) grade = "Needs Work";
        else grade = "Critical";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("score", Math.round(totalScore));
        result.put("grade", grade);
        result.put("maxScore", 850);
        result.put("savingsScore", Math.round(savingsScore));
        result.put("budgetScore", Math.round(budgetScore));
        result.put("consistencyScore", Math.round(consistencyScore));
        result.put("activityScore", Math.round(activityScore));
        result.put("savingsRate", Math.round(savingsRate));
        return result;
    }
}