package com.example.recipes.controller;

import com.example.recipes.dto.RecipeDTO;
import com.example.recipes.entity.Recipe;
import com.example.recipes.service.RecipeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@AllArgsConstructor
public class RecipeController {

    private final RecipeService service;

    // ===== Response Wrapper =====
    record ApiResponse<T>(String message, T data) {}

    // CREATE
    @PostMapping
    public ResponseEntity<ApiResponse<Recipe>> createRecipe(
            @Valid @RequestBody RecipeDTO dto,
            Authentication authentication) {

        String username = authentication.getName();
        Recipe recipe = service.createRecipe(username, dto);
        return ResponseEntity.ok(new ApiResponse<>("Recipe created successfully", recipe));
    }

    // READ ALL (with pagination & sorting)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Recipe>>> getAllRecipes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Recipe> recipes = service.getAllRecipes(pageable);
        return ResponseEntity.ok(new ApiResponse<>("Recipes fetched successfully", recipes));
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Recipe>> getRecipeById(@PathVariable Long id) {
        Recipe recipe = service.getRecipeById(id);
        return ResponseEntity.ok(new ApiResponse<>("Recipe fetched successfully", recipe));
    }

    // GET PUBLIC RECIPES
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<Recipe>>> getPublicRecipes() {
        List<Recipe> recipes = service.getPublicRecipes();
        return ResponseEntity.ok(new ApiResponse<>("Public recipes fetched successfully", recipes));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Recipe>> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody RecipeDTO dto,
            Authentication authentication) {

        String username = authentication.getName();
        Recipe updated = service.updateRecipe(username, id, dto);
        return ResponseEntity.ok(new ApiResponse<>("Recipe updated successfully", updated));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecipe(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();
        service.deleteRecipe(username, id);
        return ResponseEntity.ok(new ApiResponse<>("Recipe deleted successfully", null));
    }

    // FILTER: Vegetarian
    @GetMapping("/vegetarian")
    public ResponseEntity<ApiResponse<List<Recipe>>> filterVegetarian(@RequestParam boolean vegetarian) {
        List<Recipe> recipes = service.filterVegetarian(vegetarian);
        return ResponseEntity.ok(new ApiResponse<>("Vegetarian recipes fetched successfully", recipes));
    }

    // FILTER: Servings
    @GetMapping("/servings")
    public ResponseEntity<ApiResponse<List<Recipe>>> filterByServings(@RequestParam int servings) {
        List<Recipe> recipes = service.filterByServings(servings);
        return ResponseEntity.ok(new ApiResponse<>("Recipes filtered by servings", recipes));
    }

    // FILTER: By ingredients
    @GetMapping("/ingredients")
    public ResponseEntity<ApiResponse<List<Recipe>>> filterByIngredients(@RequestParam List<String> ingredients) {
        List<Recipe> recipes = service.filterByIngredients(ingredients);
        return ResponseEntity.ok(new ApiResponse<>("Recipes filtered by ingredients", recipes));
    }

    // FILTER: Exclude ingredients with text
    @GetMapping("/exclude-ingredients")
    public ResponseEntity<ApiResponse<List<Recipe>>> filterExcludingIngredients(
            @RequestParam List<String> ingredients,
            @RequestParam String text) {

        List<Recipe> recipes = service.filterExcludingIngredientsWithText(ingredients, text);
        return ResponseEntity.ok(new ApiResponse<>("Recipes filtered by exclusions", recipes));
    }

    // SEARCH: Instructions
    @GetMapping("/search-instructions")
    public ResponseEntity<ApiResponse<List<Recipe>>> searchInstructions(@RequestParam String text) {
        List<Recipe> recipes = service.searchInstructions(text);
        return ResponseEntity.ok(new ApiResponse<>("Recipes searched by instructions", recipes));
    }
}
