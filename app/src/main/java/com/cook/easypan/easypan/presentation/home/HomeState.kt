package com.cook.easypan.easypan.presentation.home

import androidx.compose.foundation.lazy.LazyListState
import com.cook.easypan.easypan.domain.model.Recipe

data class HomeState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val recipeListState: LazyListState = LazyListState(),
    val hasSubscribedToTopic: Boolean = false
)