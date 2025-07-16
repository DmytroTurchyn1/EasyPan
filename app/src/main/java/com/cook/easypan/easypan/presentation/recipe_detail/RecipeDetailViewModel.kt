package com.cook.easypan.easypan.presentation.recipe_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class RecipeDetailViewModel : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(RecipeDetailState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {

                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = RecipeDetailState()
        )

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

            else -> Unit
        }
    }

}