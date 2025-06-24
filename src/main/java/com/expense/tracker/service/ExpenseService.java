package com.expense.tracker.service;

import com.expense.tracker.dto.NewExpenseDTO;
import com.expense.tracker.dto.ExpenseDTO;
import com.expense.tracker.model.Category;
import com.expense.tracker.model.Expense;
import com.expense.tracker.repository.CategoryRepo;
import com.expense.tracker.repository.ExpenseRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepo expenseRepo;
    private final CategoryRepo categoryRepo;

// SAVE EXPENSE
    public ExpenseDTO saveExpense(NewExpenseDTO expenseDTO) {
        Expense expense = newDTOtoExpense(expenseDTO);

        expenseRepo.save(expense);
        return toExpenseDTO(expense);
    }

// UPDATE EXPENSE
    public ExpenseDTO updateExpense(ExpenseDTO expenseDTO, Long id) {
        Expense expense = expenseRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense with given ID doesn't exist!"));

        expense.setAmount(expenseDTO.amount());
        expense.setDescription(expenseDTO.description());
        expense.setDate(expenseDTO.date());
        expense.setCategory(getCategory(expenseDTO.categoryId()));

        return toExpenseDTO(expenseRepo.save(expense));
    }

// DELETE EXPENSE
    public void deleteExpense(Long id) {
        expenseRepo.delete(
                expenseRepo.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Expense with given ID doesn't exist!"))
        );
    }

// GET ALL EXPENSES
    public List<ExpenseDTO> getAllExpenses() {
        return expenseRepo.findAll()
                .stream()
                .map(this::toExpenseDTO)
                .toList();
    }

// GET EXPENSE BY ID
    public ExpenseDTO getExpenseById(Long id) {
        return toExpenseDTO(
                expenseRepo.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Expense with given ID doesn't exist!"))
        );
    }

// GET EXPENSES BY CATEGORY ID
    public List<ExpenseDTO> getExpensesByCategoryId(Integer id) {
        List<Expense> expenses = expenseRepo.findByCategoryId(id);

        if (expenses.isEmpty()) {
            throw new IllegalArgumentException("Category with given ID doesn't exist!");
        }

        return expenses
                .stream()
                .map(this::toExpenseDTO)
                .toList();
    }

// GET EXPENSES BY DATE RANGE
    public List<ExpenseDTO> getExpenseBetween(LocalDate start, LocalDate end) {
        return expenseRepo.findByDateRange(start, end)
                .stream()
                .map(this::toExpenseDTO)
                .toList();
    }

// GET TOTAL EXPENSES FOR A CATEGORY
    public BigDecimal getTotalExpenseByCategoryId(Integer id) {
        return expenseRepo.getTotalAmountByCategoryId(id);
    }

/*PRIVATE METHODS*/

    private Category getCategory(Integer id){
        return categoryRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category with given ID doesn't exist!"));
    }

    private Expense toExpense(ExpenseDTO dto) {
        return new Expense(
                dto.amount(),
                dto.description(),
                dto.date(),
                getCategory(dto.categoryId())
        );
    }

    private Expense newDTOtoExpense(NewExpenseDTO dto) {
        return new Expense(
                dto.amount(),
                dto.description(),
                dto.date(),
                getCategory(dto.categoryId())
        );
    }

    private ExpenseDTO toExpenseDTO(Expense expense) {
        return new ExpenseDTO(
                expense.getId(),
                expense.getAmount(),
                expense.getDescription(),
                expense.getDate(),
                expense.getCategory().getId()
        );
    }

}
