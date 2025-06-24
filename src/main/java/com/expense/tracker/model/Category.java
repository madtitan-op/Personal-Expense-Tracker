package com.expense.tracker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "categories")
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, unique = true, nullable = false)
    private String name;

    @OneToMany(
            mappedBy = "category",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true
    )
    private List<Expense> expenses;

    public void addExpense(Expense expense) {
        this.expenses.add(expense);
        expense.setCategory(this);
    }

    public void removeExpense(Expense expense) {
        this.expenses.remove(expense);
        expense.setCategory(null);
    }

    public Category() {}
    public Category(String name) {
        this.name = name;
    }
}
