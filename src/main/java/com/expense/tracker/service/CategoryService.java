package com.expense.tracker.service;

import com.expense.tracker.dto.CategoryDTO;
import com.expense.tracker.model.Category;
import com.expense.tracker.repository.CategoryRepo;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepo categoryRepo;

// CREATE NEW CATEGORY
    public CategoryDTO saveCategory(CategoryDTO dto) {
        categoryRepo.save(toCategory(dto));
        return toCategoryDTO(categoryRepo.findByNameContaining(dto.name()));
    }

// GET ALL CATEGORY
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepo.findAll();
        return categories.stream()
                .map(this::toCategoryDTO)
                .toList();
    }

// GET CATEGORY BY ID
    public CategoryDTO getCategoryById(Integer id) {
        return toCategoryDTO(
                categoryRepo.findById(id)
                        .orElseThrow(() -> new NoResultException("Category with given ID doesn't exist"))
        );
    }

// UPDATE CATEGORY
    public CategoryDTO updateCategory(Integer id, CategoryDTO dto) {
        Category category = categoryRepo.findById(id).orElse(null);
        if (category != null) {
            category.setName(dto.name());
            return toCategoryDTO(categoryRepo.save(category));
        }
        throw new NoResultException("Category with given ID doesn't exist");
    }

// DELETE CATEGORY
    public void deleteCategory(Integer id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new NoResultException("Category with given ID doesn't exist"));
        categoryRepo.delete(category);
    }

    /*PRIVATE METHODS*/
    private Category toCategory(CategoryDTO dto) {
        return new Category(dto.name());
    }

    private CategoryDTO toCategoryDTO(Category category) {
        return new CategoryDTO(category.getId(), category.getName());
    }
}
