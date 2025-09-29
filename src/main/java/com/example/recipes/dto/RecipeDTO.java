package com.example.recipes.dto;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Set;

public class RecipeDTO {

    @NotBlank(message = "Recipe name is required")
    @Size(min = 2, max = 100, message = "Recipe name must be between 2 and 100 characters")
    public String name;

    @NotNull(message = "Vegetarian flag is required")
    public boolean vegetarian;

    @Min(value = 1, message = "Servings must be at least 1")
    public int servings;

    @NotBlank(message = "Instructions are required")
    public String instructions;

    @NotEmpty(message = "At least one ingredient is required")
    public List<@NotBlank(message = "Ingredient name cannot be blank") String> ingredients;
}
