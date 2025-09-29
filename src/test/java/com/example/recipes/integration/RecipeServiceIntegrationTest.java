package com.example.recipes.integration;

import com.example.recipes.dto.RecipeDTO;
import com.example.recipes.entity.Ingredient;
import com.example.recipes.entity.Recipe;
import com.example.recipes.entity.User;
import com.example.recipes.repository.IngredientRepository;
import com.example.recipes.repository.RecipeRepository;
import com.example.recipes.repository.UserRepository;
import com.example.recipes.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RecipeServiceIntegrationTest {

    private RecipeRepository recipeRepository;
    private IngredientRepository ingredientRepository;
    private UserRepository userRepository;
    private RecipeService recipeService;

    private User testUser;

    @BeforeEach
    void setup() {
        recipeRepository = Mockito.mock(RecipeRepository.class);
        ingredientRepository = Mockito.mock(IngredientRepository.class);
        userRepository = Mockito.mock(UserRepository.class);

        recipeService = new RecipeService(recipeRepository, ingredientRepository, userRepository);

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    void testCreateAndGetRecipe() {
        RecipeDTO dto = new RecipeDTO();
        dto.name = "Pasta";
        dto.servings = 2;
        dto.vegetarian = true;
        dto.instructions = "Boil pasta.";
        dto.ingredients = List.of("Pasta", "Olive Oil");

        // Mock ingredients
        Ingredient pasta = new Ingredient();
        pasta.setId(1L);
        pasta.setName("Pasta");
        pasta.setRecipes(new HashSet<>());

        when(ingredientRepository.findByName("Pasta")).thenReturn(Optional.of(pasta));
        when(ingredientRepository.findByName("Olive Oil")).thenReturn(Optional.empty());

        // Mock recipe save
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));

        Recipe saved = recipeService.createRecipe("testuser", dto);

        assertThat(saved.getName()).isEqualTo("Pasta");
        assertThat(saved.getIngredients())
                .extracting("name")
                .containsExactlyInAnyOrder("Pasta", "Olive Oil");
        assertThat(saved.getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    void testDeleteRecipe() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("Soup");
        recipe.setUser(testUser);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        recipeService.deleteRecipe("testuser",1L );

        // No exception means deletion attempted
        Mockito.verify(recipeRepository).delete(recipe);
    }

    @Test
    void testFilterVegetarian() {
        Recipe veg = new Recipe();
        veg.setName("Salad");
        veg.setVegetarian(true);

        Recipe nonVeg = new Recipe();
        nonVeg.setName("Chicken Soup");
        nonVeg.setVegetarian(false);

        when(recipeRepository.findByVegetarian(true)).thenReturn(List.of(veg));

        List<Recipe> vegetarianRecipes = recipeService.filterVegetarian(true);
        assertThat(vegetarianRecipes).hasSize(1);
        assertThat(vegetarianRecipes.get(0).getName()).isEqualTo("Salad");
    }
}
