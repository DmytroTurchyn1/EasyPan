/*
 * Created  25/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.data.repository

import android.content.Context
import android.util.Log
import com.cook.easypan.app.dataStore
import com.cook.easypan.core.util.RECIPES_CACHE_TIMEOUT
import com.cook.easypan.easypan.data.database.FirestoreClient
import com.cook.easypan.easypan.data.mappers.toRecipe
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.first

class DefaultRecipeRepository(
    private val firestoreDataSource: FirestoreClient,
    private val context: Context
) : RecipeRepository {
    override suspend fun getRecipes(): List<Recipe> {
        val lastCacheTimeRecipes = context.dataStore.data.first().lastCacheTimeRecipes
        if (System.currentTimeMillis() - lastCacheTimeRecipes < RECIPES_CACHE_TIMEOUT) {
            Log.d("DefaultRecipeRepository", "Returning cachedRecipes from cache")
            val cachedRecipes = context.dataStore.data.first().cachedRecipes
            Log.d(
                "DefaultRecipeRepository",
                "Returning cachedRecipes state: ${cachedRecipes.isNotEmpty()}"
            )
            if (cachedRecipes.isNotEmpty()) {
                Log.d("DefaultRecipeRepository", "Returning recently cached recipes")
                return cachedRecipes.map { it.toRecipe() }
            }
        }
        val recipes = firestoreDataSource.getRecipes()

        Log.d("DefaultRecipeRepository", "Caching recipes")
        context.dataStore.updateData { appSettings ->
            appSettings.copy(
                cachedRecipes = recipes,
                lastCacheTimeRecipes = System.currentTimeMillis()
            )
        }
        return recipes.map { it.toRecipe() }
    }
}