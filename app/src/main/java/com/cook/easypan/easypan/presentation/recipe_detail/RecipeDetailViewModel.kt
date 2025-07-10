package com.cook.easypan.easypan.presentation.recipe_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

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
            else -> TODO("Handle actions")
        }
    }

}