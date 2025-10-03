package com.gourmet.recipes.mapper;

import com.gourmet.recipes.dto.RecipeDTO;
import com.gourmet.recipes.entity.Ingredient;
import com.gourmet.recipes.entity.Recipe;
import com.gourmet.recipes.repository.IngredientRepository;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RecipeMapper {

    @Mapping(target = "ingredients", source = "dto.ingredients")
    Recipe toEntity(RecipeDTO dto, @Context IngredientRepository ingredientRepo);

    @Mapping(target = "ingredients", source = "recipe.ingredients")
    RecipeDTO toDto(Recipe recipe);

    // === Custom ingredient mapping using repo ===
    default Set<Ingredient> mapIngredients(List<String> names, @Context IngredientRepository ingredientRepo) {
        if (names == null) return null;
        return names.stream()
                .map(name -> ingredientRepo.findByName(name).orElseGet(() -> {
                    Ingredient ing = new Ingredient();
                    ing.setName(name);
                    return ing;
                }))
                .collect(Collectors.toSet());
    }

    default List<String> mapIngredientNames(Set<Ingredient> ingredients) {
        if (ingredients == null) return null;
        return ingredients.stream()
                .map(Ingredient::getName)
                .collect(Collectors.toList());
    }

    // update an existing recipe in place
    @Mapping(target = "id", ignore = true)        // don’t overwrite ID
    @Mapping(target = "user", ignore = true)      // ownership handled in service
    @Mapping(target = "createdAt", ignore = true) // don’t reset created date
    @Mapping(target = "updatedAt", ignore = true) // we’ll set manually
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRecipeFromDto(RecipeDTO dto, @MappingTarget Recipe recipe, @Context IngredientRepository ingredientRepo);
}
