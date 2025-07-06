package com.dmytro_turchyn.easypan.easypan.presentation.favorite

data class FavoriteState(
    val paramOne: String = "default",
    val paramTwo: List<String> = emptyList(),
)