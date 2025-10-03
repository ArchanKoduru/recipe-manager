package com.gourmet.recipes.integration;

import com.gourmet.recipes.dto.RecipeDTO;
import com.gourmet.recipes.entity.Recipe;
import com.gourmet.recipes.entity.User;
import com.gourmet.recipes.repository.IngredientRepository;
import com.gourmet.recipes.repository.RecipeRepository;
import com.gourmet.recipes.repository.UserRepository;
import com.gourmet.recipes.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class RecipeServiceIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.33")
            .withDatabaseName("recipes")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    private User testUser;

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
    }

    @BeforeEach
    void setup() {
        recipeRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password"); // plain for testing
        userRepository.save(testUser);
    }

    @Test
    void createRecipe_success() {
        RecipeDTO dto = new RecipeDTO();
        dto.setName("Pasta");
        dto.setServings(2);
        dto.setVegetarian(true);
        dto.setInstructions("Boil pasta and add sauce");
        dto.setIngredients(List.of("Pasta", "Olive Oil"));
        dto.setPublic(true);

        Recipe created = recipeService.createRecipe("testuser", dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getUser().getUsername()).isEqualTo("testuser");
        assertThat(created.getIngredients()).extracting("name")
                .containsExactlyInAnyOrder("Pasta", "Olive Oil");
    }

    @Test
    void searchRecipes_success() {
        // Prepare recipe
        RecipeDTO dto = new RecipeDTO();
        dto.setName("Veggie Pasta");
        dto.setServings(2);
        dto.setVegetarian(true);
        dto.setInstructions("Boil pasta with veggies");
        dto.setIngredients(List.of("Pasta", "Tomato"));
        dto.setPublic(true);

        recipeService.createRecipe("testuser", dto);

        // Search
        var results = recipeService.searchRecipes(
                "Veggie",
                true,
                2,
                List.of("Pasta"),
                List.of(), // excluded ingredients
                "veggies",
                "testuser",
                false, // not admin
                PageRequest.of(0, 10)
        );

        assertThat(results.getTotalElements()).isEqualTo(1);
        Recipe r = results.getContent().get(0);
        assertThat(r.getName()).isEqualTo("Veggie Pasta");
    }
}
