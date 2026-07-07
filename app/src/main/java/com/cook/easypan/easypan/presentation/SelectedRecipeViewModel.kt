package com.cook.easypan.easypan.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SelectedRecipeViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    companion object {
        private const val KEY_SELECTED_RECIPE_ID = "selected_recipe_id"
    }

    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe = _selectedRecipe.asStateFlow()

    init {
        // After process death the recipe object is gone; restore it from the
        // (usually cached) catalog using the persisted id.
        savedStateHandle.get<String>(KEY_SELECTED_RECIPE_ID)?.let { recipeId ->
            viewModelScope.launch {
                runCatching { recipeRepository.getRecipes() }
                    .onSuccess { recipes ->
                        if (_selectedRecipe.value == null) {
                            _selectedRecipe.value = recipes.find { it.id == recipeId }
                        }
                    }
            }
        }
    }

    fun onSelectRecipe(recipe: Recipe?) {
        savedStateHandle[KEY_SELECTED_RECIPE_ID] = recipe?.id
        _selectedRecipe.value = recipe
    }
}
