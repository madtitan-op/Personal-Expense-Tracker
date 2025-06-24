package com.expense.tracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NewExpenseDTO(
        @NotNull
        @DecimalMin(value = "0.1")
        BigDecimal amount,
        @NotBlank
        String description,
        @NotNull
        @PastOrPresent
        LocalDate date,
        @NotNull
        Integer categoryId
) {}
