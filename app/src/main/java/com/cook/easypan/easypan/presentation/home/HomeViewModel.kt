/*
 * Created  14/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.domain.repository.RecipeRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
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
    private var allRecipes: List<Recipe> = emptyList()


    private val _state = MutableStateFlow(HomeState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadData()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HomeState()
        )

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val recipes = recipeRepository.getRecipes()
                allRecipes = recipes
                val filters = recipes.map { recipe ->
                    recipe.chips
                }.flatten().distinct().sorted()
                _state.update {
                    it.copy(
                        recipes = recipes,
                        filterList = filters,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to load recipes", e)
                val error =
                    if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                        AppError.NETWORK
                    } else {
                        AppError.UNKNOWN
                    }
                _state.update { it.copy(isLoading = false, error = error) }
            }
        }
    }


    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.OnRecipeClick -> {

            }

            is HomeAction.OnFilterSelected -> {
                val newFilter =
                    if (state.value.selectedFilter == action.filter) "" else action.filter
                val newRecipeList = if (newFilter.isEmpty()) {
                    allRecipes
                } else {
                    allRecipes.filter { it.chips.contains(newFilter) }
                }
                _state.update { st ->
                    st.copy(
                        selectedFilter = newFilter,
                        recipes = newRecipeList
                    )
                }
            }

            is HomeAction.OnRetryClick -> loadData()
        }
    }

}
