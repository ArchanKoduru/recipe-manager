package com.gourmet.recipes.service;

import com.gourmet.recipes.dto.RecipeDTO;
import com.gourmet.recipes.entity.Ingredient;
import com.gourmet.recipes.entity.Recipe;
import com.gourmet.recipes.entity.User;
import com.gourmet.recipes.mapper.RecipeMapper;
import com.gourmet.recipes.repository.IngredientRepository;
import com.gourmet.recipes.repository.RecipeRepository;
import com.gourmet.recipes.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RecipeServiceTest {

    private RecipeRepository recipeRepository;
    private IngredientRepository ingredientRepository;
    private UserRepository userRepository;
    private RecipeMapper recipeMapper;
    private RecipeService service;

    @BeforeEach
    void setup() {
        recipeRepository = Mockito.mock(RecipeRepository.class);
        ingredientRepository = Mockito.mock(IngredientRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        recipeMapper = Mockito.mock(RecipeMapper.class);

        service = new RecipeService(recipeRepository, ingredientRepository, userRepository, recipeMapper);
    }

    @Test
    void testCreateRecipe() {
        RecipeDTO dto = new RecipeDTO();
        dto.setName("Pasta");
        dto.setServings(2);
        dto.setVegetarian(true);
        dto.setInstructions("Boil pasta.");
        dto.setIngredients(List.of("Pasta", "Olive Oil"));

        User mockUser = new User();
        mockUser.setUsername("testUser");
        mockUser.setId(1L);
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));

        Ingredient pasta = new Ingredient();
        pasta.setName("Pasta");
        pasta.setRecipes(new HashSet<>());
        when(ingredientRepository.findByName("Pasta")).thenReturn(Optional.of(pasta));
        when(ingredientRepository.findByName("Olive Oil")).thenReturn(Optional.empty());
        when(recipeMapper.toEntity(dto, ingredientRepository)).thenAnswer(invocation -> {
            Recipe r = new Recipe();
            r.setName(dto.getName());
            r.setServings(dto.getServings());
            r.setVegetarian(dto.isVegetarian());
            r.setInstructions(dto.getInstructions());

            Set<Ingredient> ingredients = new HashSet<>();
            for (String name : dto.getIngredients()) {
                Ingredient ing = ingredientRepository.findByName(name).orElseGet(() -> {
                    Ingredient newIng = new Ingredient();
                    newIng.setName(name);
                    return newIng;
                });
                ingredients.add(ing);
            }
            r.setIngredients(ingredients);
            r.setUser(userRepository.findByUsername("testUser").get());
            return r; });

        when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));

        Recipe saved = service.createRecipe("testUser", dto);

        assertThat(saved.getName()).isEqualTo(dto.getName());
        assertThat(saved.getIngredients())
                .extracting(Ingredient::getName)
                .containsExactlyInAnyOrderElementsOf(dto.getIngredients());
        assertThat(saved.getUser().getUsername()).isEqualTo("testUser");
    }
}
