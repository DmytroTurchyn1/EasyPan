package com.dmytro_turchyn.easypan

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route

@Serializable
data object AuthGraph : Route {
    @Serializable
    data object Authentication : Route
}

@Serializable
data object AppGraph : Route {
    @Serializable
    data object Home : Route

    @Serializable
    data object Favorite : Route

    @Serializable
    data object Profile : Route
}