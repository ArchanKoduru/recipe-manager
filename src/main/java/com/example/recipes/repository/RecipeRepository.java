package com.example.recipes.repository;

import com.example.recipes.entity.Recipe;
import com.example.recipes.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByUser(User owner); // get recipes of a specific user

    List<Recipe> findByIsPublicTrue(); // get public recipes

    Page<Recipe> findAll(Pageable pageable);

    // Find recipes by vegetarian
    List<Recipe> findByVegetarian(boolean vegetarian);

    // Find recipes by servings
    List<Recipe> findByServings(int servings);

    // Search recipes by partial instruction text
    @Query("SELECT r FROM Recipe r WHERE LOWER(r.instructions) LIKE LOWER(CONCAT('%', :text, '%'))")
    List<Recipe> findByInstructionsContaining(@Param("text") String text);

    @EntityGraph(attributePaths = "ingredients")
    @Query("SELECT r FROM Recipe r WHERE r.id = :id")
    Optional<Recipe> findByIdWithIngredients(@Param("id") Long id);

    // ✅ Filter by ingredients (recipe must contain *all* given ingredients)
    @Query("SELECT r FROM Recipe r " +
            "WHERE NOT EXISTS (" +
            "  SELECT i FROM Ingredient i " +
            "  WHERE i.name IN :ingredients " +
            "  AND i NOT IN (SELECT ing FROM r.ingredients ing)" +
            ")")
    List<Recipe> findByIngredients(@Param("ingredients") List<String> ingredients);

    // ✅ Exclude recipes that contain certain ingredients, or that mention text in instructions
    @Query("SELECT r FROM Recipe r " +
            "WHERE NOT EXISTS (" +
            "  SELECT 1 FROM r.ingredients i " +
            "  WHERE i.name IN :ingredientNames" +
            ") " +
            "AND LOWER(r.instructions) NOT LIKE LOWER(CONCAT('%', :text, '%'))")
    List<Recipe> findExcludingIngredientsWithText(@Param("ingredientNames") List<String> ingredientNames,
                                                  @Param("text") String text);
}
