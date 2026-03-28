package com.springboot.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.springboot.api.entity.transactions.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDto {

    private Long id;
    @NotBlank
    private String title;
    @NotNull
    @Positive
    private Double amount;
    @NotNull
    private TransactionType type;
    private String note;
    @NotNull
    private Long categoryId;
    private String categoryName;
}