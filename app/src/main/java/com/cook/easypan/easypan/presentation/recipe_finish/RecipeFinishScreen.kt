/*
 * Created  15/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.recipe_finish

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.cook.easypan.ui.theme.EasyPanTheme

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
    if (!state.isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.recipe_finished_title),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 36.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.cooked_recipes_count, state.userFinishedRecipes),
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 10.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(64.dp))
            if (state.recipe != null) {
                SubcomposeAsyncImage(
                    model = state.recipe.titleImg,
                    contentDescription = stringResource(R.string.dish_image_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
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
                            contentDescription = "Recipe Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                )
                Text(
                    text = state.recipe.title,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 10.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

            }


            Spacer(modifier = Modifier.height(58.dp))
            EasyPanButtonPrimary(
                onClick = { onAction(RecipeFinishAction.OnFinishClick) },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.recipe_finished_button),
                )
            }
            OutlinedButton(
                onClick = {
                    if (state.recipe != null) {
                        onAction(RecipeFinishAction.OnFavoriteClick(recipe = state.recipe))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (state.isFavorite) Icons.Default.Check else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.add_to_favorite_button),
                    )
                    Text(
                        text = if (state.isFavorite) stringResource(R.string.added_to_favorite_button) else stringResource(
                            R.string.add_to_favorite_button
                        ),
                    )
                }

            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    EasyPanTheme {
        RecipeFinishScreen(
            state = RecipeFinishState(),
            onAction = {}
        )
    }
}