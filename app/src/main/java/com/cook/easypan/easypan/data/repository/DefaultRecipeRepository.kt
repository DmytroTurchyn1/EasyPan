package com.cook.easypan.easypan.data.repository

import com.cook.easypan.easypan.data.database.FirestoreClient
import com.cook.easypan.easypan.data.mappers.toRecipe
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.domain.repository.RecipeRepository

class DefaultRecipeRepository(
    private val firestoreDataSource: FirestoreClient
) : RecipeRepository {
    override suspend fun getRecipes(): List<Recipe> {
        return firestoreDataSource
            .getRecipes()
            .map { it.toRecipe() }
    }
}