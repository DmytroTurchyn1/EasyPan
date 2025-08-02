package com.cook.easypan.easypan.presentation.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
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

@Composable
fun BottomNavigationBar(
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val screens = listOf(
        BottomBarScreen.Home,
        //BottomBarScreen.Favorite,
        BottomBarScreen.Profile
    )
    val bottomBarDestination =
        screens.any { it.route::class.qualifiedName == currentDestination?.route }
    if (bottomBarDestination) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            screens.forEach { screen ->
                AddItem(
                    screen = screen,
                    currentDestination = currentDestination,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomBarScreen,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    NavigationBarItem(
        colors = NavigationBarItemColors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            disabledIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            selectedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ),
        selected = currentDestination?.route == screen.route::class.qualifiedName,
        onClick = {
            if (currentDestination?.route == screen.route::class.qualifiedName) return@NavigationBarItem
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        },
        label = {
            Text(
                text = stringResource(id = screen.title),
            )
        },
        icon = {
            Icon(
                imageVector = screen.icon,
                contentDescription = "${screen.title} icon",
                modifier = Modifier.size(24.dp)
            )
        }
    )
}