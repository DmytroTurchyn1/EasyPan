package com.cook.easypan.easypan.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cook.easypan.easypan.data.auth.GoogleAuthUiClient
import com.cook.easypan.ui.theme.EasyPanTheme


/**
 * Sets up the root navigation graph for the app, determining the start destination based on user authentication state.
 *
 * Applies the app theme, initializes navigation, and conditionally displays either the authentication flow or the main app content.
 */
@Composable
fun RootNavGraph(
    googleAuthUiClient: GoogleAuthUiClient
) {
    EasyPanTheme {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = if (googleAuthUiClient.getSignedInUser() == null) {
                Route.AuthGraph
            } else {
                Route.AppGraph
            }
        ) {
            authNavGraph(
                navController = navController,
                googleAuthUiClient = googleAuthUiClient
            )
            composable<Route.AppGraph> {
                Home(
                    googleAuthUiClient = googleAuthUiClient,
                    onSignOut = {
                        navController.popBackStack()
                        navController.navigate(Route.AuthGraph)
                    }
                )
            }
        }
    }
}

