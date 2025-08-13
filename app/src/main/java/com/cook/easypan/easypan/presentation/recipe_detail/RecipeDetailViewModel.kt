/*
 * Created  13/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.recipe_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.cook.easypan.easypan.domain.repository.UserRepository
import com.cook.easypan.easypan.presentation.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecipeDetailViewModel(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(RecipeDetailState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeFavoriteStatus()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = RecipeDetailState()
        )

    private val recipeId = savedStateHandle.toRoute<Route.RecipeDetail>().id

    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            val isFavorite = userRepository.isRecipeFavorite(recipeId = recipeId)
            _state.update {
                it.copy(
                    isFavorite = isFavorite
                )
            }
        }
    }

    fun onAction(action: RecipeDetailAction) {
        when (action) {
            is RecipeDetailAction.OnRecipeChange -> {
                _state.update {
                    it.copy(
                        recipe = action.recipe
                    )
                }
            }

            is RecipeDetailAction.OnIngredientCheck -> {
                _state.update {
                    val updatedSet =
                        if (it.onIngredientCheckClicked.contains(action.ingredientIndex)) {
                            it.onIngredientCheckClicked - action.ingredientIndex
                        } else {
                            it.onIngredientCheckClicked + action.ingredientIndex
                        }
                    it.copy(
                        onIngredientCheckClicked = updatedSet
                    )
                }
            }

            is RecipeDetailAction.OnFavoriteButtonClick -> {
                viewModelScope.launch {
                    state.value.recipe?.let { recipe ->
                        if (state.value.isFavorite) {
                            userRepository.deleteRecipeFromFavorites(recipeId = recipe.id)
                            _state.update {
                                it.copy(
                                    isFavorite = false
                                )
                            }

                        } else {
                            userRepository.addRecipeToFavorites(recipe)
                            _state.update {
                                it.copy(
                                    isFavorite = true
                                )
                            }
                        }
                    }
                }
            }

            else -> Unit
        }
    }

}