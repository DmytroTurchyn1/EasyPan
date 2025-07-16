package com.cook.easypan.easypan.presentation.navigation

import android.app.Activity.RESULT_OK
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.cook.easypan.easypan.data.auth.GoogleAuthUiClient
import com.cook.easypan.easypan.presentation.authentication.AuthenticationRoot
import com.cook.easypan.easypan.presentation.authentication.AuthenticationViewModel
import org.koin.androidx.compose.koinViewModel


/**
 * Adds the authentication navigation graph to the navigation builder, handling user sign-in flow.
 *
 * Sets up the authentication screen, manages Google sign-in intent launching and result handling,
 * and navigates to the main app graph upon successful authentication.
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    googleAuthUiClient: GoogleAuthUiClient
) {
    navigation<Route.AuthGraph>(
        startDestination = Route.Authentication
    ) {
        composable<Route.Authentication> {
            val viewModel = koinViewModel<AuthenticationViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = { result ->
                    if (result.resultCode == RESULT_OK && result.data != null) {
                        result.data.let { data ->
                            viewModel.handleSignInResult(googleAuthUiClient, data)
                        }
                    }
                }
            )

            LaunchedEffect(state.signInIntentSender) {
                state.signInIntentSender?.let { sender ->
                    launcher.launch(IntentSenderRequest.Builder(sender).build())
                }
            }

            LaunchedEffect(state.isSignInSuccessful) {
                if (state.isSignInSuccessful) {
                    navController.popBackStack()
                    navController.navigate(Route.AppGraph)
                    viewModel.resetState()
                }
            }

            AuthenticationRoot(viewModel = viewModel)
        }
    }
}