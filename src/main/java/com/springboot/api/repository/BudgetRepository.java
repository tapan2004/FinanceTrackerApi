package com.springboot.api.repository;

import com.springboot.api.entity.budgets.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUser_UserId(Long userId);
    Optional<Budget> findByUser_UserIdAndCategory_IdAndMonthAndYear(
            Long userId,
            Long categoryId,
            Integer month,
            Integer year
    );
}