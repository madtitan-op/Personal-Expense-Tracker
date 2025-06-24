package com.expense.tracker.controller;

import com.expense.tracker.dto.*;
import com.expense.tracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Expense Controller", description = "REST endpoints for managing expenses")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "Create a new expense.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Expense created successfully!"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    public ResponseEntity<?> createExpense(@Validated  @RequestBody NewExpenseDTO expenseDTO) {
        try {
            return new ResponseEntity<>(
                    expenseService.saveExpense(expenseDTO),
                    HttpStatus.CREATED
            );
        } catch (IllegalArgumentException argumentException) {
            return new ResponseEntity<>(
                    argumentException.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Something went wrong!",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Operation(summary = "Get all expenses")
    @ApiResponse(responseCode = "200", description = "List of all expenses")
    @GetMapping
    public ResponseEntity<?> getAllExpenses() {
        try {
            return new ResponseEntity<>(
                    expenseService.getAllExpenses(),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Something went wrong!",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Operation(summary = "Get expenses by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expenses found"),
            @ApiResponse(responseCode = "404", description = "No expenses found for given ID")
    })
    @GetMapping("{id}")
    public ResponseEntity<?> getExpenseById(@PathVariable Long id) {
        try {
            return new ResponseEntity<>(
                    expenseService.getExpenseById(id),
                    HttpStatus.OK
            );
        } catch (IllegalArgumentException argException) {
            return new ResponseEntity<>(
                    argException.getMessage(),
                    HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Something went wrong!",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Operation(summary = "Get expenses by category ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expenses for category found"),
            @ApiResponse(responseCode = "404", description = "No expenses found for category")
    })
    @GetMapping("category/{categoryId}")
    public ResponseEntity<?> getExpenseByCategoryId(@PathVariable Integer categoryId) {
        try {
            return new ResponseEntity<>(
                    expenseService.getExpensesByCategoryId(categoryId),
                    HttpStatus.OK
            );
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Something went wrong!",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Operation(summary = "Get expenses within a date range")
    @ApiResponse(responseCode = "200", description = "Expenses within the specified date range")
    @GetMapping("date-range")
    public ResponseEntity<?> getExpenseByDateRange(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end
            ) {
        try {
            return new ResponseEntity<>(
                    expenseService.getExpenseBetween(start, end),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Something went wrong!",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Operation(summary = "Get total expenses for a category")
    @ApiResponse(responseCode = "200", description = "Total expenses for category found")
    @GetMapping("total/category/{categoryId}")
    public ResponseEntity<?> getTotalExpenseByCategory(@PathVariable Integer categoryId) {
        try {
            return new ResponseEntity<>(
                    expenseService.getTotalExpenseByCategoryId(categoryId),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Something went wrong!",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Operation(summary = "Update an existing expense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense updated successfully"),
            @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    @PutMapping("{id}")
    public ResponseEntity<?> updateExpense(@PathVariable Long id, @Validated @RequestBody ExpenseDTO expenseDTO) {
        try {
            return new ResponseEntity<>(
                    expenseService.updateExpense(expenseDTO, id),
                    HttpStatus.OK
            );
        } catch (IllegalArgumentException argException) {
            return new ResponseEntity<>(
                    argException.getMessage(),
                    HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Something went wrong!",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Operation(summary = "Delete an expense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id) {
        try {
            expenseService.deleteExpense(id);
            return ResponseEntity.ok("Deleted!");
        } catch (IllegalArgumentException argException) {
            return new ResponseEntity<>(
                    argException.getMessage(),
                    HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Something went wrong!",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
