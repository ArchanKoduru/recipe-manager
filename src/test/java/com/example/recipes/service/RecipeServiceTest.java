package com.example.recipes.service;

import com.example.recipes.dto.RecipeDTO;
import com.example.recipes.entity.Ingredient;
import com.example.recipes.entity.Recipe;
import com.example.recipes.entity.User;
import com.example.recipes.repository.IngredientRepository;
import com.example.recipes.repository.RecipeRepository;
import com.example.recipes.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RecipeServiceTest {

    private RecipeRepository recipeRepository;
    private IngredientRepository ingredientRepository;
    private UserRepository userRepository;
    private RecipeService service;

    @BeforeEach
    void setup() {
        recipeRepository = Mockito.mock(RecipeRepository.class);
        ingredientRepository = Mockito.mock(IngredientRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        service = new RecipeService(recipeRepository, ingredientRepository, userRepository);
    }

    @Test
    void testCreateRecipe() {
        // 1️⃣ Prepare DTO
        RecipeDTO dto = new RecipeDTO();
        dto.name = "Pasta";
        dto.servings = 2;
        dto.vegetarian = true;
        dto.instructions = "Boil pasta.";
        dto.ingredients = List.of("Pasta", "Olive Oil");

        // 2️⃣ Mock user lookup
        User mockUser = new User();
        mockUser.setUsername("testUser");
        mockUser.setId(1L);
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));

        // 3️⃣ Mock ingredient repository
        Ingredient pasta = new Ingredient();
        pasta.setId(1L);
        pasta.setName("Pasta");
        pasta.setRecipes(new HashSet<>());

        when(ingredientRepository.findByName("Pasta")).thenReturn(Optional.of(pasta));
        when(ingredientRepository.findByName("Olive Oil")).thenReturn(Optional.empty());

        // 4️⃣ Mock recipe save
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 5️⃣ Call service
        Recipe saved = service.createRecipe("testUser", dto);

        // 6️⃣ Verify
        assertThat(saved.getName()).isEqualTo("Pasta");
        assertThat(saved.getIngredients())
                .extracting("name")
                .containsExactlyInAnyOrder("Pasta", "Olive Oil");
        assertThat(saved.getUser().getUsername()).isEqualTo("testUser");
    }
}
