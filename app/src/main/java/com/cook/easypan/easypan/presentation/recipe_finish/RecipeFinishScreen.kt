/*
 * Created  15/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.recipe_finish

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import com.cook.easypan.R
import com.cook.easypan.core.presentation.EasyPanButtonPrimary
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.presentation.recipe_finish.components.CookedRecipesBox
import com.cook.easypan.easypan.presentation.recipe_finish.components.RecipeCompleteChip
import com.cook.easypan.ui.theme.EasyPanTheme

private const val HERO_ASPECT_RATIO = 412f / 254f

@Composable
fun RecipeFinishRoot(
    viewModel: RecipeFinishViewModel,
    onFinishClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RecipeFinishScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is RecipeFinishAction.OnFinishClick -> onFinishClick()
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
private fun RecipeFinishScreen(
    state: RecipeFinishState,
    onAction: (RecipeFinishAction) -> Unit,
) {
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.recipe != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(HERO_ASPECT_RATIO)
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
                Box(
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                0.75f to Color.Transparent,
                                0.95f to MaterialTheme.colorScheme.background
                            )
                        )
                )
            }

            RecipeCompleteChip(
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.recipe_finished_title),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = state.recipe.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(
                    R.string.recipe_meta_short,
                    state.recipe.cookMinutes,
                    state.recipe.difficulty
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            CookedRecipesBox {
                Text(
                    text = "${state.userFinishedRecipes}",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = stringResource(R.string.recipes_cooked),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            EasyPanButtonPrimary(
                onClick = { onAction(RecipeFinishAction.OnFinishClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = stringResource(R.string.recipe_finished_button),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            OutlinedButton(
                onClick = {
                    if (state.recipe != null) {
                        onAction(RecipeFinishAction.OnFavoriteClick(recipe = state.recipe))
                    }
                },
                enabled = !state.isFavoriteUpdating,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (state.isFavorite) {
                        stringResource(R.string.added_to_favorite_button)
                    } else {
                        stringResource(R.string.add_to_favorite_button)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private val previewRecipe = Recipe(
    id = "1",
    title = "Banana Bread",
    ingredients = listOf("Banana", "Flour"),
    allergies = emptyList(),
    preparationMinutes = 10,
    cookMinutes = 18,
    chips = emptyList(),
    difficulty = "Medium",
    instructions = emptyList(),
    titleImg = "",
)

@Preview(showBackground = true, device = "spec:width=412dp,height=917dp")
@Composable
private fun RecipeFinishScreenPreview() {
    EasyPanTheme {
        RecipeFinishScreen(
            state = RecipeFinishState(
                recipe = previewRecipe,
                userFinishedRecipes = 1
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, device = "spec:width=412dp,height=917dp")
@Composable
private fun RecipeFinishScreenFavoritePreview() {
    EasyPanTheme {
        RecipeFinishScreen(
            state = RecipeFinishState(
                recipe = previewRecipe,
                userFinishedRecipes = 12,
                isFavorite = true
            ),
            onAction = {}
        )
    }
}
