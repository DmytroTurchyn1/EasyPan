/*
 * Created  8/9/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.navigation

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cook.easypan.core.presentation.snackBar.SnackBarController
import com.cook.easypan.core.util.ObserveAsEvents
import com.cook.easypan.easypan.domain.repository.UserRepository
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject


@Composable
fun RootNavGraph() {
    val navController = rememberNavController()
    val userRepository: UserRepository by inject(UserRepository::class.java)
    val startDestination = remember {
        if (userRepository.isUserSignedIn()) Route.AppGraph else Route.AuthGraph
    }
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    ObserveAsEvents(
        SnackBarController.events,
        snackBarHostState
    ) { event ->
        scope.launch {
            snackBarHostState.currentSnackbarData?.dismiss()
            val result = snackBarHostState.showSnackbar(
                message = event.message,
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


}

