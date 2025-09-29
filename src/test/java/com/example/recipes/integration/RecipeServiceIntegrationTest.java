package com.example.recipes.integration;

import com.example.recipes.dto.RecipeDTO;
import com.example.recipes.entity.Recipe;
import com.example.recipes.repository.RecipeRepository;
import com.example.recipes.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RecipeServiceIntegrationTest {

    @Autowired
    private RecipeService service;

    @Autowired
    private RecipeRepository recipeRepository;

    @Test
    void testCreateAndFindRecipe() {
        RecipeDTO dto = new RecipeDTO();
        dto.name = "Test Pasta";
        dto.vegetarian = true;
        dto.servings = 2;
        dto.instructions = "Boil pasta. Add sauce.";
        dto.ingredients = Set.of("Pasta", "Tomato");

        Recipe created = service.createRecipe(dto);

        Recipe fetched = service.getRecipeById(created.getId());
        assertEquals("Test Pasta", fetched.getName());
        assertEquals(2, fetched.getServings());
        assertEquals(2, fetched.getIngredients().size()); // now works
    }

    @Test
    void testFilterVegetarian() {
        // create 2 recipes
        RecipeDTO dto1 = new RecipeDTO();
        dto1.name = "Veggie";
        dto1.vegetarian = true;
        dto1.servings = 2;
        dto1.instructions = "Cook veggie";
        dto1.ingredients = Set.of("Carrot");
        service.createRecipe(dto1);

        RecipeDTO dto2 = new RecipeDTO();
        dto2.name = "Chicken";
        dto2.vegetarian = false;
        dto2.servings = 4;
        dto2.instructions = "Cook chicken";
        dto2.ingredients = Set.of("Chicken");
        service.createRecipe(dto2);

        List<Recipe> vegRecipes = service.filterVegetarian(true);
        assertEquals(1, vegRecipes.size());
        assertTrue(vegRecipes.get(0).isVegetarian());
    }
}
