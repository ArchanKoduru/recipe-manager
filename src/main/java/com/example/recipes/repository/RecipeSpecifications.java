package com.example.recipes.repository;


import com.example.recipes.entity.Ingredient;
import com.example.recipes.entity.Recipe;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;

import java.util.List;

public class RecipeSpecifications {

    public static Specification<Recipe> hasName(String name) {
        return (root, query, builder) ->
                name == null ? null :
                        builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Recipe> isVegetarian(Boolean vegetarian) {
        return (root, query, builder) ->
                vegetarian == null ? null :
                        builder.equal(root.get("vegetarian"), vegetarian);
    }

    public static Specification<Recipe> hasServings(Integer servings) {
        return (root, query, builder) ->
                servings == null ? null :
                        builder.equal(root.get("servings"), servings);
    }

    public static Specification<Recipe> hasIngredients(List<String> ingredients) {
        return (root, query, builder) -> {
            if (ingredients == null || ingredients.isEmpty()) return null;

            query.distinct(true);
            // Correct join with entity types
            Join<Recipe, Ingredient> ingredientJoin = root.join("ingredients");

            return ingredientJoin.get("name").in(ingredients);
        };
    }

    public static Specification<Recipe> excludesIngredients(List<String> ingredients) {
        return (root, query, builder) -> {
            if (ingredients == null || ingredients.isEmpty()) return null;
            Join<Recipe, Ingredient> join = root.join("ingredients");
            return builder.not(join.get("name").in(ingredients));
        };
    }

    public static Specification<Recipe> isPublic() {
        return (root, query, builder) ->
                builder.isTrue(root.get("isPublic"));
    }

    public static Specification<Recipe> belongsToUser(String username) {
        return (root, query, builder) ->
                builder.equal(root.get("user").get("username"), username);
    }

    public static Specification<Recipe> instructionsContains(String text) {
        return (root, query, builder) ->
                text == null ? null :
                        builder.like(builder.lower(root.get("instructions")), "%" + text.toLowerCase() + "%");
    }
}

