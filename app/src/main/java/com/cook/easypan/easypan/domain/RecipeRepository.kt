package com.cook.easypan.easypan.domain

interface RecipeRepository {
    suspend fun getRecipes(): List<Recipe>

}