package com.cook.easypan.easypan.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.cook.easypan.R

sealed class BottomBarScreen(
    val title: Int,
    val route: Route,
    val icon: ImageVector
) {
    object Home : BottomBarScreen(
        route = Route.Home,
        title = R.string.home_bar_title,
        icon = Icons.Default.Home
    )

    object Favorite : BottomBarScreen(
        route = Route.Favorite,
        title = R.string.favorite_bar_title,
        icon = Icons.Default.Favorite
    )

    object Profile : BottomBarScreen(
        route = Route.Profile,
        title = R.string.profile_bar_title,
        icon = Icons.Default.Person
    )
}