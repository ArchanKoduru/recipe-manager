package com.example.recipes.service;

import com.example.recipes.dto.RecipeDTO;
import com.example.recipes.entity.Ingredient;
import com.example.recipes.entity.Recipe;
import com.example.recipes.exception.RecipeNotFoundException;
import com.example.recipes.repository.IngredientRepository;
import com.example.recipes.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;

    public RecipeService(RecipeRepository recipeRepository, IngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
    }

    @Transactional
    public Recipe createRecipe(RecipeDTO dto) {
        Recipe recipe = new Recipe();
        recipe.setName(dto.name);
        recipe.setVegetarian(dto.vegetarian);
        recipe.setServings(dto.servings);
        recipe.setInstructions(dto.instructions);
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());


        Set<Ingredient> ingredients = dto.ingredients.stream()
                .map(name -> ingredientRepository.findByName(name).orElseGet(() -> {
                    Ingredient ing = new Ingredient();
                    ing.setName(name);
                    return ing;
                }))
                .collect(Collectors.toSet());

        recipe.setIngredients(ingredients);
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());

        return recipeRepository.save(recipe);
    }

    public Recipe getRecipeById(Long id) {
        return recipeRepository.findByIdWithIngredients(id)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe with ID " + id + " not found"));
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public List<Recipe> filterVegetarian(boolean vegetarian) {
        return recipeRepository.findByVegetarian(vegetarian);
    }

    public List<Recipe> filterByServings(int servings) {
        return recipeRepository.findByServings(servings);
    }

    public List<Recipe> filterByIngredients(List<String> ingredients) {
        return recipeRepository.findByIngredients(ingredients);
    }

    public List<Recipe> filterExcludingIngredientsWithText(List<String> ingredients, String text) {
        if (ingredients == null || ingredients.isEmpty()) {
            return recipeRepository.findAll();
        }
        return recipeRepository.findExcludingIngredientsWithText(ingredients, text);
    }

    public List<Recipe> searchInstructions(String text) {
        return recipeRepository.findByInstructionsContaining(text);
    }

    @Transactional
    public Recipe updateRecipe(Long id, RecipeDTO dto) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        recipe.setName(dto.name);
        recipe.setVegetarian(dto.vegetarian);
        recipe.setServings(dto.servings);
        recipe.setInstructions(dto.instructions);

        // Update ingredients
        Set<Ingredient> ingredients = dto.ingredients.stream()
                .map(name -> ingredientRepository.findByName(name).orElseGet(() -> {
                    Ingredient ing = new Ingredient();
                    ing.setName(name);
                    return ing;
                }))
                .collect(Collectors.toSet());
        recipe.setIngredients(ingredients);

        recipe.setUpdatedAt(LocalDateTime.now());

        return recipeRepository.save(recipe);
    }

    @Transactional
    public void deleteRecipe(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("Cannot delete. Recipe with ID " + id + " does not exist"));
        recipeRepository.delete(recipe);
    }
}
