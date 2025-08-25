/*
 * Created  25/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.recipe_finish

import android.util.Log
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

class RecipeFinishViewModel(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(RecipeFinishState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                updateCookedRecipes()
                observeFavoriteStatus()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = RecipeFinishState()
        )


    private val recipeId = savedStateHandle.toRoute<Route.RecipeFinish>().id
    private fun updateCookedRecipes() {
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                val recipesCooked = user?.data?.recipesCooked ?: 0
                _state.update {
                    it.copy(
                        userFinishedRecipes = recipesCooked + 1,
                        isLoading = false
                    )
                }
                launch { userRepository.updateUserData() }

            } catch (e: Exception) {
                Log.e("Recipe Finish Screen", "Error updating user data: ${e.message}")
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun observeFavoriteStatus(recipeId: String = this.recipeId) {
        viewModelScope.launch {
            val isFavorite = userRepository.isRecipeFavorite(recipeId = recipeId)
            _state.update {
                it.copy(
                    isFavorite = isFavorite
                )
            }
        }
    }

    fun onAction(action: RecipeFinishAction) {
        when (action) {
            is RecipeFinishAction.OnRecipeChange -> {
                _state.update {
                    it.copy(
                        recipe = action.recipe,
                        isLoading = false
                    )
                }
                observeFavoriteStatus(action.recipe.id)
            }

            is RecipeFinishAction.OnFavoriteClick -> {
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