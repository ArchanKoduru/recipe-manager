package com.gourmet.recipes.repository;

import com.gourmet.recipes.entity.Recipe;
import com.gourmet.recipes.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long>, JpaSpecificationExecutor<Recipe> {

    // Get recipes of a specific user
    List<Recipe> findByUser(User owner);

    // Get all public recipes
    List<Recipe> findByIsPublicTrue();

    // Fetch recipe by ID including ingredients (avoids N+1 problem)
    @EntityGraph(attributePaths = "ingredients")
    Optional<Recipe> findById(Long id);
}