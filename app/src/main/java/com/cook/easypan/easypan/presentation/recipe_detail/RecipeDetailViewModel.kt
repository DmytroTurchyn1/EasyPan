/*
 * Created  18/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.recipe_detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.cook.easypan.R
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.core.domain.Result
import com.cook.easypan.core.presentation.snackBar.SnackBarController
import com.cook.easypan.core.presentation.snackBar.SnackBarEvent
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
            runCatching {
                userRepository.isRecipeFavorite(recipeId = recipeId)
            }.onSuccess { isFavorite ->
                _state.update {
                    it.copy(
                        isFavorite = isFavorite
                    )
                }
            }.onFailure {
                Log.e(
                    "RecipeDetailViewModel",
                    "Error observing favorite status: ${it.message}"
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

            is RecipeDetailAction.OnFavoriteButtonClick -> toggleFavorite()

            else -> Unit
        }
    }

    private fun toggleFavorite() {
        if (_state.value.isFavoriteUpdating) return
        viewModelScope.launch {
            _state.update { it.copy(isFavoriteUpdating = true) }
            val wasFavorite = _state.value.isFavorite
            val result = if (wasFavorite) {
                userRepository.deleteRecipeFromFavorites(recipeId = recipeId)
            } else {
                _state.value.recipe?.let { recipe ->
                    userRepository.addRecipeToFavorites(recipe)
                } ?: Result.Failure(AppError.UNKNOWN)
            }
            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isFavorite = !wasFavorite,
                            isFavoriteUpdating = false
                        )
                    }
                }

                is Result.Failure -> {
                    _state.update { it.copy(isFavoriteUpdating = false) }
                    SnackBarController.sendEvent(
                        SnackBarEvent(messageRes = R.string.error_favorite_update)
                    )
                }
            }
        }
    }
}
