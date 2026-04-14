package com.springboot.api.repository;

import com.springboot.api.dto.response.SummaryDto;import com.springboot.api.entity.transactions.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUser_UserId(Long userId);
    List<Transaction> findTop5ByUser_UserIdOrderByCreatedAtDesc(Long userId);
    Page<Transaction> findByUser_UserId(Long userId, Pageable pageable);
    Optional<Transaction> findByIdAndUser_UserId(Long id, Long userId);
    List<Transaction> findByUser_UserIdAndSuspiciousTrue(Long userId);

    //MONTHLY EXPENSE CHART
    @Query("""
            SELECT MONTH(t.createdAt) as month, SUM(t.amount)
            FROM Transaction t
            WHERE t.user.userId = :userId AND t.type = 'EXPENSE'
            GROUP BY MONTH(t.createdAt)
            """)
    List<Object[]> getMonthlyExpenses(@Param("userId") Long userId);

    //INCOME VS EXPENSE SUMMARY
    @Query("""
            SELECT SUM(CASE WHEN t.type='INCOME' THEN t.amount ELSE 0 END),
                   SUM(CASE WHEN t.type='EXPENSE' THEN t.amount ELSE 0 END)
            FROM Transaction t
            WHERE t.user.userId = :userId
            """)
    SummaryDto getIncomeExpenseSummary(Long userId);

    //DATE RANGE FILTER
    @Query("""
            SELECT t FROM Transaction t
            WHERE t.user.userId = :userId
            AND t.createdAt BETWEEN :startDate AND :endDate
            ORDER BY t.createdAt DESC
            """)
    List<Transaction> findTransactionsByDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    //TOTAL INCOME
    @Query("""
            SELECT SUM(t.amount)
            FROM Transaction t
            WHERE t.user.userId = :userId
            AND t.type = 'INCOME'
            """)
    Double getTotalIncome(Long userId);

    //TOTAL EXPENSE
    @Query("""
            SELECT SUM(t.amount)
            FROM Transaction t
            WHERE t.user.userId = :userId
            AND t.type = 'EXPENSE'
            """)
    Double getTotalExpense(Long userId);

    //TOP SPENDING CATEGORY
    @Query("""
            SELECT c.name, SUM(t.amount)
            FROM Transaction t
            JOIN t.category c
            WHERE t.user.userId = :userId
            AND t.type = 'EXPENSE'
            GROUP BY c.name
            ORDER BY SUM(t.amount) DESC
            """)
    List<Object[]> getTopExpenseCategory(Long userId);

    //AVERAGE MONTHLY EXPENSE
    @Query("""
        SELECT SUM(t.amount)
        FROM Transaction t
        WHERE t.user.userId = :userId
        AND t.type = 'EXPENSE'
        GROUP BY MONTH(t.createdAt)
        """)
    List<Double> getMonthlyExpenseTotals(Long userId);

    //AVERAGE EXPENSE PER TRANSACTION
    @Query("""
            SELECT AVG(t.amount)
            FROM Transaction t
            WHERE t.user.userId = :userId
            AND t.type = 'EXPENSE'
            """)
    Double getAverageExpense(Long userId);

    //MONTHLY CATEGORY EXPENSE
    @Query("""
                SELECT SUM(t.amount)
                FROM Transaction t
                WHERE t.user.userId = :userId
                AND t.category.id = :categoryId
                AND t.type = 'EXPENSE'
                AND MONTH(t.createdAt) = :month
                AND YEAR(t.createdAt) = :year
            """)
    Double sumCategoryExpenseForMonth(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("month") int month,
            @Param("year") int year
    );

    // TOTAL TRANSACTIONS BY CATEGORY
    @Query("""
            SELECT COUNT(t) FROM Transaction t
            WHERE t.user.userId = :userId AND t.category.id = :categoryId
            """)
    Long countTransactionsByCategory(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    // TOTAL SPENT/EARNED BY CATEGORY 
    @Query("""
            SELECT SUM(t.amount) FROM Transaction t
            WHERE t.user.userId = :userId AND t.category.id = :categoryId
            """)
    Double sumTransactionsByCategory(@Param("userId") Long userId, @Param("categoryId") Long categoryId);
}