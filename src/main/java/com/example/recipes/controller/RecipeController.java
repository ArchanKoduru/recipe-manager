package com.example.recipes.controller;

import com.example.recipes.dto.RecipeDTO;
import com.example.recipes.entity.Ingredient;
import com.example.recipes.entity.Recipe;
import com.example.recipes.repository.IngredientRepository;
import com.example.recipes.service.RecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService service;
    private final IngredientRepository ingredientRepository;
    public RecipeController(RecipeService service, IngredientRepository ingredientRepository) {
        this.service = service;
        this.ingredientRepository = ingredientRepository;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@RequestBody RecipeDTO dto) {
        Recipe created = service.createRecipe(dto);
        return ResponseEntity.ok(created);
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<Recipe>> getAllRecipes() {
        return ResponseEntity.ok(service.getAllRecipes());
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable Long id) {
        Recipe recipe = service.getRecipeById(id);
        return ResponseEntity.ok(recipe);
    }

    // UPDATE
// UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Recipe> updateRecipe(@PathVariable Long id,
                                               @RequestBody RecipeDTO dto) {
        Recipe updated = service.updateRecipe(id, dto); // delegate to service
        return ResponseEntity.ok(updated);
    }



    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        service.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    // FILTER: Vegetarian
    @GetMapping("/vegetarian")
    public ResponseEntity<List<Recipe>> filterVegetarian(@RequestParam boolean vegetarian) {
        return ResponseEntity.ok(service.filterVegetarian(vegetarian));
    }

    // FILTER: Servings
    @GetMapping("/servings")
    public ResponseEntity<List<Recipe>> filterByServings(@RequestParam int servings) {
        return ResponseEntity.ok(service.filterByServings(servings));
    }

    // FILTER: By ingredients
    @GetMapping("/ingredients")
    public ResponseEntity<List<Recipe>> filterByIngredients(@RequestParam List<String> ingredients) {
        return ResponseEntity.ok(service.filterByIngredients(ingredients));
    }

    // FILTER: Exclude ingredients with text
    @GetMapping("/exclude-ingredients")
    public ResponseEntity<List<Recipe>> filterExcludingIngredients(
            @RequestParam List<String> ingredients,
            @RequestParam String text) {
        return ResponseEntity.ok(service.filterExcludingIngredientsWithText(ingredients, text));
    }

    // SEARCH: Instructions
    @GetMapping("/search-instructions")
    public ResponseEntity<List<Recipe>> searchInstructions(@RequestParam String text) {
        return ResponseEntity.ok(service.searchInstructions(text));
    }
}
