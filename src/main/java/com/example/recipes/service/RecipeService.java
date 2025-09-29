package com.example.recipes.service;

import com.example.recipes.dto.RecipeDTO;
import com.example.recipes.entity.Ingredient;
import com.example.recipes.entity.Recipe;
import com.example.recipes.entity.User;
import com.example.recipes.exception.InvalidInputException;
import com.example.recipes.exception.RecipeNotFoundException;
import com.example.recipes.exception.UnauthorizedActionException;
import com.example.recipes.repository.IngredientRepository;
import com.example.recipes.repository.RecipeRepository;
import com.example.recipes.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RecipeService {

    private static final Logger logger = LoggerFactory.getLogger(RecipeService.class);
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final UserRepository userRepository;

    public RecipeService(RecipeRepository recipeRepository, IngredientRepository ingredientRepository, UserRepository userRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a recipe for the given user (looked up by username).
     */
    @Transactional
    public Recipe createRecipe(String username, RecipeDTO dto) {
        logger.info("Creating recipe for user {}: {}", username, dto.name);

        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + username));

        Recipe recipe = mapDtoToEntity(dto);
        recipe.setUser(owner);
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());

        owner.addRecipe(recipe);

        return recipeRepository.save(recipe);
    }

   public List<Recipe> getPublicRecipes() {
        return recipeRepository.findByIsPublicTrue();
    }

    public Recipe getRecipeById(Long id) {
        return recipeRepository.findByIdWithIngredients(id)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe with ID " + id + " not found"));
    }

    public Page<Recipe> getAllRecipes(Pageable pageable) {
        return recipeRepository.findAll(pageable);
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
    public Recipe updateRecipe(String username, Long recipeId, RecipeDTO dto) {
        logger.info("Updating recipe {} for user {}", recipeId, username);

        // Fetch the user performing the update
        User owner = getUser(username);

        // Fetch the recipe
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found: " + recipeId));

        // Check ownership
        if (!recipe.getUser().equals(owner)) {
            throw new UnauthorizedActionException("You do not own this recipe");
        }

        // Update recipe fields
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
    public void deleteRecipe(String username, Long recipeId) {
        logger.info("Deleting recipe {} for user {}", recipeId, username);
        User owner = getUser(username);

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe with ID " + recipeId + " not found"));

        if (!recipe.getUser().equals(owner)) {
            throw new UnauthorizedActionException("You cannot delete another user’s recipe");
        }

        recipeRepository.delete(recipe);
    }

    // ===== Helpers =====

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidInputException("User not found: " + username));
    }

    private Recipe mapDtoToEntity(RecipeDTO dto) {
        Recipe recipe = new Recipe();
        recipe.setName(dto.name);
        recipe.setVegetarian(dto.vegetarian);
        recipe.setServings(dto.servings);
        recipe.setInstructions(dto.instructions);

        Set<Ingredient> ingredients = dto.ingredients.stream()
                .map(name -> ingredientRepository.findByName(name).orElseGet(() -> {
                    Ingredient ing = new Ingredient();
                    ing.setName(name);
                    return ing;
                }))
                .collect(Collectors.toSet());
        recipe.setIngredients(ingredients);

        return recipe;
    }
}
