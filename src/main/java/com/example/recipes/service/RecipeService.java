package com.example.recipes.service;

import com.example.recipes.dto.RecipeDTO;
import com.example.recipes.entity.Ingredient;
import com.example.recipes.entity.Recipe;
import com.example.recipes.entity.User;
import com.example.recipes.exception.InvalidInputException;
import com.example.recipes.exception.RecipeNotFoundException;
import com.example.recipes.exception.UnauthorizedActionException;
import com.example.recipes.mapper.RecipeMapper;
import com.example.recipes.repository.RecipeSpecifications;
import com.example.recipes.repository.IngredientRepository;
import com.example.recipes.repository.RecipeRepository;
import org.springframework.data.jpa.domain.Specification;
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
@Transactional
public class RecipeService {

    private static final Logger logger = LoggerFactory.getLogger(RecipeService.class);
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final UserRepository userRepository;
    private final RecipeMapper mapper;

    public RecipeService(RecipeRepository recipeRepository, IngredientRepository ingredientRepository, UserRepository userRepository, RecipeMapper mapper) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    /**
     * Creates a recipe for the given user (looked up by username).
     */
    @Transactional
    public Recipe createRecipe(String username, RecipeDTO dto) {
        logger.info("Creating recipe for user {}: {}", username, dto.getName());

        User owner = getUser(username);

        Recipe recipe = mapper.toEntity(dto, ingredientRepository);
        recipe.setUser(owner);
        recipe.setPublic(dto.isPublic());
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());

        return recipeRepository.save(recipe);
    }

     public Recipe getRecipeById(Long id, String username, boolean isAdmin) {
         Recipe recipe = recipeRepository.findById(id)
                 .orElseThrow(() -> new RecipeNotFoundException("Recipe with ID " + id + " not found"));

         if (!isAdmin && !recipe.isPublic() && !recipe.getUser().getUsername().equals(username)) {
             throw new UnauthorizedActionException("You cannot view this recipe");
         }

         return recipe;
    }

    public String getRecipeOwnerUsername(Long recipeId) {
        return recipeRepository.findById(recipeId)
                .map(r -> r.getUser().getUsername())
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found: " + recipeId));
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

        // MapStruct handles update mapping
        mapper.updateRecipeFromDto(dto, recipe, ingredientRepository);
        recipe.setPublic(dto.isPublic());
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

    public Page<Recipe> searchRecipes(String name,
                                      Boolean vegetarian,
                                      Integer servings,
                                      List<String> ingredients,
                                      List<String> excludedIngredients,
                                      String text,
                                      String username,
                                      boolean isAdmin,
                                      Pageable pageable) {

        Specification<Recipe> spec = Specification
                .where(RecipeSpecifications.hasName(name))
                .and(RecipeSpecifications.isVegetarian(vegetarian))
                .and(RecipeSpecifications.hasServings(servings))
                .and(RecipeSpecifications.hasIngredients(ingredients))
                .and(RecipeSpecifications.instructionsContains(text))
                .and(RecipeSpecifications.excludesIngredients(excludedIngredients));

        if (!isAdmin) {
            spec = spec.and(
                    RecipeSpecifications.belongsToUser(username)
                            .or(RecipeSpecifications.isPublic())
            );
        }

        return recipeRepository.findAll(spec, pageable);
    }
    // ===== Helpers =====

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidInputException("User not found: " + username));
    }

}
