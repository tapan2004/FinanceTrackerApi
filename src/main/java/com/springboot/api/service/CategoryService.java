package com.springboot.api.service;

import com.springboot.api.dto.response.CategoryDto;
import com.springboot.api.entity.categories.Category;
import com.springboot.api.entity.categories.CategoryType;
import com.springboot.api.entity.users.User;
import com.springboot.api.repository.CategoryRepository;
import com.springboot.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    private Category toEntity(CategoryDto dto, User user) {
        return Category.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .type(dto.getType())
                .user(user)
                .build();
    }

    private CategoryDto toDto(Category category) {
        Long userId = category.getUser().getUserId();
        Long count = transactionRepository.countTransactionsByCategory(userId, category.getId());
        Double total = transactionRepository.sumTransactionsByCategory(userId, category.getId());

        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .icon(category.getIcon())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .transactionCount(count != null ? count : 0L)
                .totalAmount(total != null ? total : 0.0)
                .build();
    }

    public CategoryDto saveCategory(CategoryDto dto, String email) {
        User user = userService.findByEmail(email);
        if (categoryRepository.existsByNameAndUser_UserId(dto.getName(), user.getUserId())) {
            throw new RuntimeException("Category already exists");
        }
        Category category = toEntity(dto, user);
        Category saved = categoryRepository.save(category);
        return toDto(saved);
    }

    public List<CategoryDto> getCategoriesForCurrentUser(String email) {
        User byEmail = userService.findByEmail(email);
        List<Category> categories = categoryRepository.findByUser_UserId(byEmail.getUserId());
        return categories
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<CategoryDto> getCategoriesByType(CategoryType type, String email) {
        User user = userService.findByEmail(email);
        List<Category> categories = categoryRepository.findByTypeAndUser_UserId(type, user.getUserId());
        return categories
                .stream()
                .map(this::toDto)
                .toList();
    }

    public CategoryDto UpdateCategory(Long id, CategoryDto dto, String email) {
        User byEmail = userService.findByEmail(email);
        Category category = categoryRepository.findByIdAndUser_UserId(id, byEmail.getUserId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setName(dto.getName());
        category.setType(dto.getType());
        category.setIcon(dto.getIcon());
        Category updated = categoryRepository.save(category);
        return toDto(updated);
    }

    public void deleteCategory(Long id, String email) {
        User user = userService.findByEmail(email);
        Category category = categoryRepository
                .findByIdAndUser_UserId(id, user.getUserId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        categoryRepository.delete(category);
    }
}