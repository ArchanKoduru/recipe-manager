package com.gourmet.recipes.controller;

import com.gourmet.recipes.dto.RecipeDTO;
import com.gourmet.recipes.entity.Recipe;
import com.gourmet.recipes.service.RecipeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Recipe>> getRecipeById(@PathVariable Long id,
                                                             Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Recipe recipe = service.getRecipeById(id, auth.getName(), isAdmin);
        return ResponseEntity.ok(new ApiResponse<>("Recipe fetched successfully", recipe));
    }

    // UPDATE
    @PreAuthorize("hasRole('ADMIN') or #auth.name == @recipeService.getRecipeOwnerUsername(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Recipe>> updateRecipe(
            @PathVariable Long id,
            @RequestBody RecipeDTO recipeDto,
            Authentication auth) {

        Recipe updated = service.updateRecipe(auth.getName(), id, recipeDto);
        return ResponseEntity.ok(new ApiResponse<>("Recipe updated successfully", updated));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecipe(
            @PathVariable Long id,
            Authentication authentication) {

        service.deleteRecipe(authentication.getName(), id);
        return ResponseEntity.ok(new ApiResponse<>("Recipe deleted successfully", null));
    }

    // ===== SEARCH / FILTER =====
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<Recipe>>> searchRecipes(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean vegetarian,
            @RequestParam(required = false) Integer servings,
            @RequestParam(required = false) List<String> ingredients,
            @RequestParam(required = false) List<String> excludedIngredients,
            @RequestParam(required = false) String text,
            Authentication authentication,
            Pageable pageable) {

        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Page<Recipe> result = service.searchRecipes(
                name, vegetarian, servings,
                ingredients, excludedIngredients, text,
                authentication.getName(), isAdmin, pageable
        );

        return ResponseEntity.ok(new ApiResponse<>("Recipes fetched successfully", result));
    }
}
