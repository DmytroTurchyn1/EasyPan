package com.dmytro_turchyn.easypan

import kotlinx.serialization.Serializable

sealed interface Route{
    @Serializable
    data object AppGraph : Route

    @Serializable
    data object Authentication : Route

    @Serializable
    data object Home : Route
}