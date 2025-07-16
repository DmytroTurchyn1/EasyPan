package com.cook.easypan.easypan.domain

interface RecipeRepository {
    /**
 * Retrieves a list of recipes asynchronously.
 *
 * @return A list of available recipes.
 */
suspend fun getRecipes(): List<Recipe>

}