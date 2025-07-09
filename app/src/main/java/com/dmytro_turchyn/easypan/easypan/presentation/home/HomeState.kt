package com.dmytro_turchyn.easypan.easypan.presentation.home

import com.dmytro_turchyn.easypan.easypan.domain.Recipe

data class HomeState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = true
)