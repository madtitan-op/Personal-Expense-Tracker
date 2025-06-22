package com.expense.tracker.dto;

public record AuthenticationRequestDTO(
        String username,
        String password
) {
}
