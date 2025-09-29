package com.example.recipes.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "recipe")
@Data
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean vegetarian;

    @Column(nullable = false)
    private int servings;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "recipe_ingredient",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private Set<Ingredient> ingredients = new HashSet<>();

    // ===== Helper Methods =====

    public void setUser(User user) {
        this.user = user;
        if (user != null && !user.getRecipes().contains(this)) {
            user.getRecipes().add(this);
        }
    }

    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
        if (!ingredient.getRecipes().contains(this)) {
            ingredient.getRecipes().add(this);
        }
    }

    public void removeIngredient(Ingredient ingredient) {
        ingredients.remove(ingredient);
        ingredient.getRecipes().remove(this);
    }

    // ===== Lifecycle Callbacks =====

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
