package com.cook.easypan.easypan.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cook.easypan.easypan.domain.repository.UserRepository
import org.koin.java.KoinJavaComponent.inject


@Composable
fun RootNavGraph() {
    val navController = rememberNavController()
    val userRepository: UserRepository by inject(UserRepository::class.java)
    NavHost(
        navController = navController,
        startDestination = if (userRepository.isUserSignedIn()) {
            Route.AppGraph
        } else {
            Route.AuthGraph
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
                    userRepository.signOut()
                }
            )
        }
    }

}

