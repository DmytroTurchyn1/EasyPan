package com.cook.easypan.easypan.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {

    @Serializable
    data object AuthGraph : Route()

    @Serializable
    data object Authentication : Route()


    @Serializable
    data object AppGraph : Route()

    @Serializable
    data object Home : Route()

    @Serializable
    data object Favorite : Route()

    @Serializable
    data object Profile : Route()

    @Serializable
    data class RecipeDetail(val id: String) : Route()

    @Serializable
    data object RecipeStep : Route()

    @Serializable
    data class RecipeFinish(val id: String) : Route()
}