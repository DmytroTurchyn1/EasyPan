package com.cook.easypan.easypan.data.repository

import com.cook.easypan.easypan.data.database.FirestoreClient
import com.cook.easypan.easypan.data.mappers.toRecipe
import com.cook.easypan.easypan.domain.Recipe
import com.cook.easypan.easypan.domain.RecipeRepository

class DefaultRecipeRepository(
    private val firestoreDataSource: FirestoreClient
) : RecipeRepository {
    /**
     * Retrieves all recipes from the Firestore "Recipes" collection.
     *
     * @return A list of `Recipe` domain objects.
     */
    override suspend fun getRecipes(): List<Recipe> {
        return firestoreDataSource
            .getCollection("Recipes")
            .map { it.toRecipe() }
    }
}