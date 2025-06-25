package com.expense.tracker.dto;

public record RegistrationRequestDTO(
        String username,
        String password,
        String role
) {
}
