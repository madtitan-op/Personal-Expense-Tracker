package com.expense.tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "expenses",
        indexes = @Index(name = "idx_expense_date", columnList = "expense_date")
)
@Data
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(unique = true, nullable = false)
    private String description;

    @Column(name = "expense_date", columnDefinition = "DATE", nullable = false)
    private LocalDate date;

    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    //Constructor
    public Expense() {}
    public Expense(BigDecimal amount, String description, LocalDate date, Category category) {
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.category = category;
    }
}
