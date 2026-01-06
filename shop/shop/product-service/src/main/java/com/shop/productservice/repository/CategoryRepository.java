package com.shop.productservice.repository;

import com.shop.productservice.entity.Category;

//This is the generic interface of Spring Data JPA that provides all the basic CRUD methods
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//Null handling 
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    //a category with a name may or may not exist
    boolean existsByName(String name);
}
