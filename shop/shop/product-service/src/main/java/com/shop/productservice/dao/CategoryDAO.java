package com.shop.productservice.dao;

import com.shop.productservice.entity.Category;
import com.shop.productservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

//provides abstraction layer between Service and Repository
@Component
@RequiredArgsConstructor
public class CategoryDAO {
    
    private final CategoryRepository categoryRepository;

    // Save or update a category in the database
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    // Find a category by its ID
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    // Find a category by its name
    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }

    // Retrieve all categories from the database
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    // Delete a category by its ID
    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }

    // Check if a category exists by its ID
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }

    // Check if a category exists by its name
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}
