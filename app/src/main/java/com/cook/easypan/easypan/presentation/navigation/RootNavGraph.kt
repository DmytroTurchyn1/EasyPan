package com.cook.easypan.easypan.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cook.easypan.easypan.data.auth.GoogleAuthUiClient
import com.cook.easypan.ui.theme.EasyPanTheme


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
            )
            composable<Route.AppGraph> {
                Home(
                    onSignOut = {
                        navController.navigate(Route.AuthGraph) {
                            popUpTo(Route.AppGraph) {
                                inclusive = true
                            }
                        }
                        googleAuthUiClient.signOut()
                    }
                )
            }
        }
    }
}

