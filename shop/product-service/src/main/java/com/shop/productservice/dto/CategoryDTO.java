package com.shop.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Data Transfer Object for Category - used to transfer data between API layer and Service layer
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    
    private Long id;

    // Category name - required field with max 100 characters
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    // Category description - optional field with max 500 characters
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
