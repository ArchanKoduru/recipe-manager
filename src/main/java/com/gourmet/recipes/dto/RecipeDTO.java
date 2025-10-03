package com.gourmet.recipes.dto;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class RecipeDTO {

    @NotBlank(message = "Recipe name is required")
    @Size(min = 2, max = 100, message = "Recipe name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Vegetarian flag is required")
    private boolean vegetarian;

    @Min(value = 1, message = "Servings must be at least 1")
    private int servings;

    @NotBlank(message = "Instructions are required")
    private String instructions;

    @NotEmpty(message = "At least one ingredient is required")
    private List<@NotBlank(message = "Ingredient name cannot be blank") String> ingredients;

    private boolean isPublic;
}
