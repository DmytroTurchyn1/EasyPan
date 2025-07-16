package com.cook.easypan.easypan.presentation

import androidx.lifecycle.ViewModel
import com.cook.easypan.easypan.domain.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SelectedRecipeViewModel : ViewModel() {
    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe = _selectedRecipe.asStateFlow()

    /**
     * Updates the currently selected recipe.
     *
     * Sets the selected recipe state to the provided [recipe], or clears the selection if null.
     */
    fun onSelectRecipe(recipe: Recipe?) {
        _selectedRecipe.value = recipe
    }
}