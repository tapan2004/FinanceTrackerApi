package com.springboot.api.repository;

import com.springboot.api.entity.categories.Category;
import com.springboot.api.entity.categories.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUser_UserId(Long userId);

    Optional<Category> findByIdAndUser_UserId(Long id, Long userId);

    List<Category> findByTypeAndUser_UserId(CategoryType type, Long userId);

    Boolean existsByNameAndUser_UserId(String name, Long userId);

    Optional<Category> findByNameAndUser_UserId(String name, Long userId);
}