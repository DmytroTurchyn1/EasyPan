package com.cook.easypan.easypan.presentation.recipe_detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.cook.easypan.easypan.presentation.recipe_detail.components.IngredientsItem
import com.cook.easypan.easypan.presentation.recipe_detail.components.RecipeChip
import com.cook.easypan.easypan.presentation.recipe_detail.components.RecipeItem
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
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
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
            }
        }
    ) { innerPadding ->
        if (state.recipe != null) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = state.recipe.titleImg,
                        contentDescription = stringResource(R.string.dish_image_description),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            CircularProgressIndicator()
                        },
                        error = {
                            Image(
                                painter = painterResource(R.drawable.auth_img),
                                contentDescription = "Recipe Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp)
                ) {
                    Text(
                        text = state.recipe.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 20.dp, bottom = 20.dp),
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
                    Text(
                        text = stringResource(R.string.ingredients_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 20.dp, bottom = 20.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        state.recipe.ingredients.forEachIndexed { index, ingredient ->
                            IngredientsItem(
                                text = ingredient,
                                checked = state.onIngredientCheckClicked.contains(index),
                                onCheckClick = {
                                    onAction(RecipeDetailAction.OnIngredientCheck(index))
                                }
                            )
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