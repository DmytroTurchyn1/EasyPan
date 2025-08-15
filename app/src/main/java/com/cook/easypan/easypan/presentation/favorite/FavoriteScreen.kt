/*
 * Created  15/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.favorite

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cook.easypan.R
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.presentation.home.components.RecipeList
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun FavoriteRoot(
    viewModel: FavoriteViewModel,
    onRecipeClick: (Recipe) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    FavoriteScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is FavoriteAction.OnRecipeClick -> {
                    onRecipeClick(action.recipe)
                }
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
private fun FavoriteScreen(
    state: FavoriteState,
    onAction: (FavoriteAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.favoriteRecipes.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_favorite_recipes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Text(
                text = stringResource(R.string.favorite_title),
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            RecipeList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                recipes = state.favoriteRecipes,
                onRecipeClick = { recipe ->
                    onAction(FavoriteAction.OnRecipeClick(recipe))
                },
                scrollState = rememberLazyListState()
            )

        }


    }

}

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    EasyPanTheme {
        FavoriteScreen(
            state = FavoriteState(),
            onAction = {}
        )
    }
}