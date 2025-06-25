package com.expense.tracker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

public record CategoryDTO(
        @Min(value = 1)
        Integer id,
        @NotEmpty
        String name
) {
}
