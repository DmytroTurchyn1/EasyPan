/*
 * Created  18/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.home

import android.Manifest.permission.POST_NOTIFICATIONS
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cook.easypan.R
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.presentation.home.components.RecipeFilterBar
import com.cook.easypan.easypan.presentation.home.components.RecipeList
import com.cook.easypan.ui.theme.EasyPanTheme


@Composable
fun HomeRoot(
    viewModel: HomeViewModel,
    onRecipeClick: (Recipe) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.onPermissionResult(
                isGranted = isGranted
            )
        }
    )

    HomeScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is HomeAction.OnRecipeClick -> {

                    if (!viewModel.checkNotificationPermission(context)) {
                        when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                                notificationPermissionLauncher.launch(POST_NOTIFICATIONS)
                            }

                            else -> {

                                viewModel.onPermissionResult(isGranted = false)
                            }
                        }
                    } else {
                        viewModel.onPermissionResult(isGranted = true)
                    }

                    onRecipeClick(action.recipe)
                }

                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
private fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center

            ) {
                CircularProgressIndicator()
            }

        } else if (state.recipes.isNotEmpty()) {
            Text(
                text = stringResource(R.string.home_title),
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            RecipeFilterBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                onFilterSelected = { onAction(HomeAction.OnFilterSelected(it)) },
                filters = state.filterList,
                selected = state.selectedFilter
            )
            RecipeList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp),
                recipes = state.recipes,
                onRecipeClick = { recipe ->
                    onAction(HomeAction.OnRecipeClick(recipe))
                },
                scrollState = state.recipeListState
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.error_recipe),
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun Preview() {
    EasyPanTheme {
        HomeScreen(
            state = HomeState(),
            onAction = {}
        )
    }
}