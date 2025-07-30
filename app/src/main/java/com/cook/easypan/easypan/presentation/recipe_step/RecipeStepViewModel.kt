package com.cook.easypan.easypan.presentation.recipe_step

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class RecipeStepViewModel : ViewModel() {

    private var hasLoadedInitialData = false


    private val _state = MutableStateFlow(RecipeStepState())


    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                _state.update {
                    it.copy(
                        isLoading = true,
                    )
                }

                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = RecipeStepState()
        )


    fun onAction(action: RecipeStepAction) {
        val step = _state.value.recipe?.instructions?.size.let { size ->
            1f / (size?.toFloat() ?: 1f)
        }
        when (action) {
            is RecipeStepAction.OnNextClick -> {
                _state.update {
                    it.copy(
                        step = it.step + 1,
                        progressBar = it.progressBar + step
                    )
                }
            }

            is RecipeStepAction.OnPreviousClick -> {
                if (_state.value.step > 0) {
                    _state.update {
                        it.copy(
                            step = it.step - 1,
                            progressBar = it.progressBar - step
                        )
                    }
                }
            }

            is RecipeStepAction.OnRecipeChange -> {

                _state.update {
                    it.copy(
                        recipe = action.recipe,
                        isLoading = false,
                        progressBar = 1f / action.recipe.instructions.size.toFloat()
                    )
                }
            }

            is RecipeStepAction.OnDismissDialog -> {
                _state.update {
                    it.copy(
                        isDialogShowing = false
                    )
                }
            }

            is RecipeStepAction.OnShowDialog -> {
                _state.update {
                    it.copy(
                        isDialogShowing = true
                    )
                }
            }

            is RecipeStepAction.OnCancelClick -> {
                _state.update {
                    it.copy(
                        isDialogShowing = false
                    )
                }
            }

            else -> Unit
        }
    }

}