/*
 * Created  15/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.recipe_detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import com.cook.easypan.R
import com.cook.easypan.core.presentation.EasyPanButtonPrimary
import com.cook.easypan.easypan.presentation.recipe_detail.components.BottomSheet
import com.cook.easypan.easypan.presentation.recipe_detail.components.IngredientsButton
import com.cook.easypan.easypan.presentation.recipe_detail.components.IngredientsItem
import com.cook.easypan.easypan.presentation.recipe_detail.components.RecipeChip
import com.cook.easypan.easypan.presentation.recipe_detail.components.RecipeItem
import com.cook.easypan.easypan.presentation.recipe_detail.components.SelectedIngredientsChip
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun RecipeDetailRoot(
    viewModel: RecipeDetailViewModel,
    onBackClick: () -> Unit,
    onStartClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RecipeDetailScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is RecipeDetailAction.OnBackClick -> onBackClick()
                is RecipeDetailAction.OnStartRecipeClick -> onStartClick()
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeDetailScreen(
    state: RecipeDetailState,
    onAction: (RecipeDetailAction) -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        bottomBar = {
            if (state.recipe != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                ) {
                    EasyPanButtonPrimary(
                        onClick = {
                            onAction(RecipeDetailAction.OnStartRecipeClick)
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.start_cooking_button),
                            modifier = Modifier
                                .fillMaxWidth(),
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        },
        topBar = {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = { onAction(RecipeDetailAction.OnBackClick) },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.go_back),
                        tint = MaterialTheme.colorScheme.inverseSurface
                    )
                }
                if (state.recipe != null) {
                    IconButton(
                        onClick = { onAction(RecipeDetailAction.OnFavoriteButtonClick(state.recipe.id)) },
                        enabled = !state.isFavoriteUpdating,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            tint = Color.Red,
                            contentDescription = if (state.isFavorite) stringResource(R.string.delete_from_favorite_button_description) else stringResource(
                                R.string.add_to_favorite_button_description
                            ),
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val scope = rememberCoroutineScope()

        val sheetState = rememberModalBottomSheetState()
        if (state.recipe != null) {

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = state.recipe.titleImg,
                        contentDescription = stringResource(R.string.dish_image_description),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        },
                        error = {
                            Image(
                                painter = painterResource(R.drawable.auth_img),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = state.recipe.title,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(top = 20.dp, bottom = 10.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    RecipeItem(
                        title = stringResource(R.string.preparation_title),
                        text = "${state.recipe.preparationMinutes} min",
                    )
                    RecipeItem(
                        title = stringResource(R.string.cook_title),
                        text = "${state.recipe.cookMinutes} min",
                    )
                    RecipeItem(
                        title = stringResource(R.string.difficulty_title),
                        text = state.recipe.difficulty,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(top = 14.dp)
                            .wrapContentSize(Alignment.Center)
                    ) {
                        state.recipe.chips.forEach { text ->
                            RecipeChip(
                                text = text,
                                modifier = Modifier
                                    .padding(2.dp)
                            )
                        }
                    }

                    IngredientsButton(
                        modifier = Modifier.padding(10.dp),
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        text = stringResource(R.string.ingredients_button),
                        onClick = {
                            onAction(RecipeDetailAction.OnIngredientsButtonClick)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.ingredients_button)
                        )
                    }
                    if (state.showBottomSheet) {
                        BottomSheet(
                            onDismiss = {
                                onAction(RecipeDetailAction.OnBottomSheetDismiss)
                            },
                            sheetState = sheetState
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()

                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 20.dp, end = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.ingredients_title),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    SelectedIngredientsChip(
                                        modifier = Modifier
                                            .padding(end = 10.dp),
                                        selected = state.selectedIngredients,
                                        total = state.recipe.ingredients.size

                                    )
                                }
                                state.recipe.ingredients.forEachIndexed { index, ingredient ->
                                    IngredientsItem(
                                        modifier = Modifier
                                            .padding(start = 20.dp, end = 20.dp, top = 6.dp),
                                        text = ingredient.name,
                                        quantity = ingredient.quantity.ifEmpty { null },
                                        checked = state.onIngredientCheckClicked.contains(index),
                                        onCheckClick = {
                                            onAction(RecipeDetailAction.OnIngredientCheck(index))
                                        }
                                    )
                                }
                            }
                        }
                    }

                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.error_recipe),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

    }
}


@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    EasyPanTheme {
        RecipeDetailScreen(
            state = RecipeDetailState(),
            onAction = {}
        )
    }
}