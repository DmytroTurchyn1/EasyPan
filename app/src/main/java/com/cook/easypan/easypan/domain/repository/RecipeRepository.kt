package com.cook.easypan.easypan.domain.repository

import com.cook.easypan.easypan.domain.model.Recipe

interface RecipeRepository {
    suspend fun getRecipes(): List<Recipe>

}