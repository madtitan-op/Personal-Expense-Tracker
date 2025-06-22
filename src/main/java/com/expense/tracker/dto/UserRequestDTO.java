package com.expense.tracker.dto;

public record UserRequestDTO(
        String username,
        String password,
        String role
) {
}
