package com.cook.easypan.easypan.presentation.home

import com.cook.easypan.easypan.domain.Recipe

data class HomeState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)