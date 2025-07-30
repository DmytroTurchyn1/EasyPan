package com.cook.easypan.easypan.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.cook.easypan.easypan.presentation.authentication.AuthenticationRoot
import com.cook.easypan.easypan.presentation.authentication.AuthenticationViewModel
import org.koin.androidx.compose.koinViewModel


fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
) {
    navigation<Route.AuthGraph>(
        startDestination = Route.Authentication
    ) {
        composable<Route.Authentication> {
            val viewModel = koinViewModel<AuthenticationViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(state.isSignInSuccessful) {
                if (state.isSignInSuccessful) {
                    navController.popBackStack()
                    navController.navigate(Route.AppGraph)
                    viewModel.resetState()
                }
            }

            AuthenticationRoot(
                viewModel = viewModel,
            )
        }
    }
}