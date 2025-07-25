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


fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
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
                            viewModel.handleSignInResult(data = data)
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