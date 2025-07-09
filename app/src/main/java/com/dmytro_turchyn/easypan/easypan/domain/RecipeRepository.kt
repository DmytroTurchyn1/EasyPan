package com.dmytro_turchyn.easypan.easypan.domain

interface RecipeRepository {
    suspend fun getRecipes(): List<Recipe>

}