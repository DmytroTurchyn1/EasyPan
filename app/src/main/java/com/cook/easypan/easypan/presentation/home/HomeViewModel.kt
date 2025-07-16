package com.cook.easypan.easypan.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.easypan.domain.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private var hasLoadedInitialData = false


    private val _state = MutableStateFlow(HomeState())
    val state = _state
        .onStart {

            if (!hasLoadedInitialData) {
                loadRecipes()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HomeState()
        )

    /**
     * Loads recipes from the repository and updates the UI state to reflect loading and completion.
     *
     * Sets the loading indicator to true, fetches recipes asynchronously, and updates the state with the retrieved recipes and loading status.
     */
    private fun loadRecipes() {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    recipes = recipeRepository.getRecipes(),
                    isLoading = false
                )
            }
        }
    }

    /**
     * Handles UI actions related to the home screen.
     *
     * @param action The action to process, such as a recipe click event.
     */
    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.OnRecipeClick -> {

            }
        }
    }

}