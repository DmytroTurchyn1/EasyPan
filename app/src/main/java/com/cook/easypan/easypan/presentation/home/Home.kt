package com.cook.easypan.easypan.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cook.easypan.R
import com.cook.easypan.easypan.domain.Recipe
import com.cook.easypan.easypan.presentation.home.components.RecipeList
import com.cook.easypan.ui.theme.EasyPanTheme


/**
 * Root composable for the home screen, connecting UI state from the [HomeViewModel] to the [HomeScreen].
 *
 * Collects the current home screen state and handles user actions, including recipe selection via [onRecipeClick].
 *
 * @param viewModel The view model providing home screen state and action handling.
 * @param onRecipeClick Callback invoked when a recipe is selected.
 */
@Composable
fun HomeRoot(
    viewModel: HomeViewModel,
    onRecipeClick: (Recipe) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is HomeAction.OnRecipeClick -> onRecipeClick(action.recipe)
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

/**
 * Displays the home screen UI, showing a loading indicator or a list of recipes based on the current state.
 *
 * Renders a top bar with the screen title. If loading, shows a centered progress indicator; otherwise, displays the list of recipes with click handling.
 *
 * @param state The current UI state for the home screen.
 * @param onAction Callback for handling user actions such as recipe selection.
 */
@Composable
private fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
) {
    val recipeListState = rememberLazyListState()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),

        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.home_title),
                    modifier = Modifier
                        .padding(bottom = 10.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center

            ) {
                CircularProgressIndicator()
            }

        } else if (state.recipes.isNotEmpty()) {
            RecipeList(
                modifier = Modifier.padding(innerPadding),
                recipes = state.recipes,
                onRecipeClick = { recipe ->
                    onAction(HomeAction.OnRecipeClick(recipe))
                },
                scrollState = recipeListState
            )
        }

    }
}

/**
 * Displays a preview of the HomeScreen composable with default state and theme for design-time visualization.
 */
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