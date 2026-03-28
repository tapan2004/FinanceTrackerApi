package com.springboot.api.controller;


import com.springboot.api.dto.response.CategoryDto;
import com.springboot.api.entity.categories.CategoryType;
import com.springboot.api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequestMapping("/api/categories")
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> saveCategory(@RequestBody CategoryDto categoryDto, Principal principal) {
        String email = principal.getName();
        CategoryDto saved = categoryService.saveCategory(categoryDto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories(Principal principal) {
        String email = principal.getName();
        List<CategoryDto> categories = categoryService.getCategoriesForCurrentUser(email);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/type")
    public ResponseEntity<List<CategoryDto>> getCategoriesByType(
            @RequestParam CategoryType type,
            Principal principal) {
        String email = principal.getName();
        List<CategoryDto> categories =
                categoryService.getCategoriesByType(type, email);
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryDto categoryDto,
            Principal principal) {
        String name = principal.getName();
        CategoryDto updated = categoryService.UpdateCategory(id, categoryDto, name);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        categoryService.deleteCategory(id, email);
        return ResponseEntity.ok("Category deleted successfully");
    }
}