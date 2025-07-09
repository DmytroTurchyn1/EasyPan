package com.dmytro_turchyn.easypan.easypan.data.repository

import com.dmytro_turchyn.easypan.easypan.data.database.FirestoreClient
import com.dmytro_turchyn.easypan.easypan.data.mappers.toRecipe
import com.dmytro_turchyn.easypan.easypan.domain.Recipe
import com.dmytro_turchyn.easypan.easypan.domain.RecipeRepository

class DefaultRecipeRepository(
    private val firestoreDataSource: FirestoreClient
): RecipeRepository {
    override suspend fun getRecipes(): List<Recipe> {
        return firestoreDataSource
            .getCollection("Recipes")
            .map { it.toRecipe()}
    }
}