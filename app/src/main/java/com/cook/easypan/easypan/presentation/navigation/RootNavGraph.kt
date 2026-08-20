/*
 * Created  8/9/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cook.easypan.core.presentation.snackBar.SnackBarController
import com.cook.easypan.core.util.ObserveAsEvents
import com.cook.easypan.easypan.domain.repository.UserRepository
import com.cook.easypan.easypan.presentation.onboarding.OnboardingRoot
import kotlinx.coroutines.launch
import org.koin.compose.koinInject


@Composable
fun RootNavGraph() {
    val userRepository = koinInject<UserRepository>()
    val hasCompletedOnboarding by userRepository.hasCompletedOnboarding()
        .collectAsStateWithLifecycle(initialValue = null)

    val completedOnboarding = hasCompletedOnboarding
    if (completedOnboarding == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        return
    }

    RootNavHost(
        userRepository = userRepository,
        startDestination = when {
            !completedOnboarding -> Route.Onboarding
            userRepository.isUserSignedIn() -> Route.AppGraph
            else -> Route.AuthGraph
        }
    )
}

@Composable
private fun RootNavHost(
    userRepository: UserRepository,
    startDestination: Route,
) {
    val navController = rememberNavController()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    ObserveAsEvents(
        SnackBarController.events,
        snackBarHostState
    ) { event ->
        scope.launch {
            snackBarHostState.currentSnackbarData?.dismiss()
            val message = event.message
                ?: event.messageRes?.let(context::getString)
                ?: return@launch
            val result = snackBarHostState.showSnackbar(
                message = message,
                actionLabel = event.action?.name,
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                event.action?.action?.invoke()
            }
        }
    }
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { _ ->
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable<Route.Onboarding> {
                OnboardingRoot(
                    onFinished = {
                        val next = if (userRepository.isUserSignedIn()) {
                            Route.AppGraph
                        } else {
                            Route.AuthGraph
                        }
                        navController.navigate(next) {
                            popUpTo<Route.Onboarding> {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
            authNavGraph(
                navController = navController,
            )
            composable<Route.AppGraph> {
                Home(
                    onSignOut = {
                        scope.launch {
                            userRepository.signOut()
                            navController.navigate(Route.AuthGraph) {
                                popUpTo(Route.AppGraph) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
