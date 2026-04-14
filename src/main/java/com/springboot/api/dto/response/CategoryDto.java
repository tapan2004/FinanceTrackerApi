package com.springboot.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.springboot.api.entity.categories.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDto {
    private Long id;

    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "Type is required")
    private CategoryType type;

    private String icon;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Aggregation Fields
    private Long transactionCount;
    private Double totalAmount;
}