package com.shop.productservice.service;

import com.shop.productservice.dao.CategoryDAO;
import com.shop.productservice.dto.CategoryDTO;
import com.shop.productservice.entity.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// Service layer for Category logic; handles validation, conversion, and transactions
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    // DAO layer for database access
    private final CategoryDAO categoryDAO;

    // Create a new category - validates uniqueness of name before creation
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        log.info("Creating new category: {}", categoryDTO.getName());

        // Check if category name already exists to prevent duplicates
        if (categoryDAO.existsByName(categoryDTO.getName())) {
            throw new IllegalArgumentException("Category with name '" + categoryDTO.getName() + "' already exists");
        }

        // Convert DTO to Entity
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());

        // Save to database
        Category savedCategory = categoryDAO.save(category);
        log.info("Category created with ID: {}", savedCategory.getId());

        // Convert Entity back to DTO and return
        return convertToDTO(savedCategory);
    }

    // Retrieve a single category by ID - throws exception if not found
    public CategoryDTO getCategoryById(Long id) {
        log.info("Fetching category with ID: {}", id);
        Category category = categoryDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));
        return convertToDTO(category);
    }

    // Retrieve all categories from the database
    public List<CategoryDTO> getAllCategories() {
        log.info("Fetching all categories");
        return categoryDAO.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Update an existing category - validates name uniqueness if changed
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        log.info("Updating category with ID: {}", id);

        // Find the category to update
        Category category = categoryDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));

        // If name is being changed, check if new name already exists
        if (!category.getName().equals(categoryDTO.getName()) && categoryDAO.existsByName(categoryDTO.getName())) {
            throw new IllegalArgumentException("Category with name '" + categoryDTO.getName() + "' already exists");
        }

        // Update category fields
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());

        // Save changes to database
        Category updatedCategory = categoryDAO.save(category);
        log.info("Category updated: {}", updatedCategory.getId());

        return convertToDTO(updatedCategory);
    }

    // Delete a category by ID - throws exception if not found
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);

        // Check if category exists before attempting delete
        if (!categoryDAO.existsById(id)) {
            throw new IllegalArgumentException("Category not found with ID: " + id);
        }

        categoryDAO.deleteById(id);
        log.info("Category deleted: {}", id);
    }

    // Convert Category entity to CategoryDTO for API responses
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }
}
