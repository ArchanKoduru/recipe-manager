package com.example.recipes.service;

import com.example.recipes.dto.RecipeDTO;
import com.example.recipes.entity.Ingredient;
import com.example.recipes.entity.Recipe;
import com.example.recipes.repository.IngredientRepository;
import com.example.recipes.repository.RecipeRepository;
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
    private RecipeService service;

    @BeforeEach
    void setup() {
        recipeRepository = Mockito.mock(RecipeRepository.class);
        ingredientRepository = Mockito.mock(IngredientRepository.class);
        service = new RecipeService(recipeRepository, ingredientRepository);
    }

    @Test
    void testCreateRecipe() {
        RecipeDTO dto = new RecipeDTO();
        dto.name = "Pasta";
        dto.servings = 2;
        dto.vegetarian = true;
        dto.instructions = "Boil pasta.";
        dto.ingredients = Set.of("Pasta", "Olive Oil");

        when(ingredientRepository.findByName("Pasta")).thenReturn(Optional.of(new Ingredient(1L, "Pasta", new HashSet<>())));
        when(ingredientRepository.findByName("Olive Oil")).thenReturn(Optional.empty());
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));

        Recipe saved = service.createRecipe(dto);

        assertThat(saved.getName()).isEqualTo("Pasta");
        assertThat(saved.getIngredients()).extracting("name").containsExactlyInAnyOrder("Pasta", "Olive Oil");
    }
}
