package com.cook.easypan.easypan.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarScreen(
    val title: String,
    val route: Route,
    val icon: ImageVector
) {
    object Home : BottomBarScreen(
        route = Route.Home,
        title = "Home",
        icon = Icons.Default.Home
    )

    object Favorite : BottomBarScreen(
        route = Route.Favorite,
        title = "Favorite",
        icon = Icons.Default.Favorite
    )

    object Profile : BottomBarScreen(
        route = Route.Profile,
        title = "Profile",
        icon = Icons.Default.Person
    )
}