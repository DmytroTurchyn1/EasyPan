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
                    // One atomic call: popping first and navigating after is order-dependent, and
                    // when the pop fails (Authentication is the NavHost start destination) the
                    // navigate stacks AppGraph on top of a live auth entry, leaving the sign-in
                    // screen reachable with back from Home.
                    navController.navigate(Route.AppGraph) {
                        popUpTo<Route.AuthGraph> { inclusive = true }
                        launchSingleTop = true
                    }
                    viewModel.resetState()
                }
            }

            AuthenticationRoot(
                viewModel = viewModel,
            )
        }
    }
}