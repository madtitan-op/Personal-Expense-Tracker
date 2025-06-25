package com.expense.tracker.controller;

import com.expense.tracker.dto.CategoryDTO;
import com.expense.tracker.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Category Controller", description = "REST endpoints for managing categories")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/category")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Create a new category.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Expense created successfully!"),
            @ApiResponse(responseCode = "409", description = "Invalid request data")
    })
    @PostMapping("add")
    public ResponseEntity<?> createCategory( @RequestBody CategoryDTO dto) {
        try {
            return new ResponseEntity<>(
                    categoryService.saveCategory(dto),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Category already exists",
                    HttpStatus.CONFLICT
            );
        }
    }

    @Operation(summary = "Get all categories")
    @ApiResponse(responseCode = "200", description = "List of all categories")
    @GetMapping("all")
    public ResponseEntity<?> getAllCategories() {
        List<CategoryDTO> categoryDTOs = categoryService.getAllCategories();
        if (!categoryDTOs.isEmpty()) {
            return new ResponseEntity<>(categoryDTOs, HttpStatus.OK);
        }
        return new ResponseEntity<>("Categories not added yet", HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Get category by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "No category found for given ID")
    })
    @GetMapping("{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Integer id) {
        try {
            return new ResponseEntity<>(
                    categoryService.getCategoryById(id),
                    HttpStatus.OK
            );
        } catch (NoResultException e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    @Operation(summary = "Update an existing category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PutMapping("{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Integer id, @RequestBody CategoryDTO categoryDTO) {
        try {
            return new ResponseEntity<>(
                    categoryService.updateCategory(id, categoryDTO),
                    HttpStatus.OK
            );
        } catch (NoResultException e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    @Operation(summary = "Delete a category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        try {
            categoryService.deleteCategory(id);
            return new ResponseEntity<>(
                    "Deleted Successfully",
                    HttpStatus.OK
            );
        } catch (NoResultException e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.NOT_FOUND
            );
        }
    }
}
