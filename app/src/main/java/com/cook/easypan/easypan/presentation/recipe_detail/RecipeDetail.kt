package com.cook.easypan.easypan.presentation.recipe_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import coil3.compose.AsyncImage
import com.cook.easypan.R
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
            when(action){
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {
                Button(
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
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    ) { innerPadding ->
        if(state.recipe != null){

            Column(
                modifier = Modifier
                    .padding(innerPadding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(300.dp)
                ){
                    AsyncImage(
                        model = state.recipe.titleImg,
                        contentDescription = "Recipe Image",
                        placeholder = painterResource(R.drawable.ic_launcher_background),
                        error = painterResource(R.drawable.auth_img),
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    IconButton(
                        onClick = {onAction(RecipeDetailAction.OnBackClick)},
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .statusBarsPadding()
                    ){
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back),
                            tint = Color.Black
                        )
                    }
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
                            .padding(top = 20.dp, bottom = 20.dp)
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
                    ){
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
                        color = Color.White,
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
        }else{
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.error_recipe)
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