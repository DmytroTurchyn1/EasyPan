package com.cook.easypan.easypan.presentation.recipe_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.cook.easypan.R
import com.cook.easypan.easypan.presentation.recipe_detail.components.RecipeChip
import com.cook.easypan.easypan.presentation.recipe_detail.components.RecipeItem
import com.cook.easypan.easypan.presentation.recipe_detail.components.ingredientsItem
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun RecipeDetailRoot(
    viewModel: RecipeDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RecipeDetailScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
private fun RecipeDetailScreen(
    state: RecipeDetailState,
    onAction: (RecipeDetailAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if(state.recipe != null){
            AsyncImage(
                model = state.recipe.titleImg,
                contentDescription = "Recipe Image",
                placeholder = painterResource(R.drawable.ic_launcher_background),
                error = painterResource(R.drawable.auth_img),
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.012f)
            )
            Text(
                text = state.recipe.title,
                modifier = Modifier
                    .weight(1f)
            )
            RecipeItem(
                title = stringResource(R.string.preparation_title),
                text = "${state.recipe.preparationMinutes} min"
            )
            RecipeItem(
                title = stringResource(R.string.cook_title),
                text = "${state.recipe.cookMinutes} min"
            )
            RecipeItem(
                title = stringResource(R.string.difficulty_title),
                text = "${state.recipe.difficulty}"
            )
            FlowRow(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
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
                modifier = Modifier
                    .weight(1f)
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = state.recipe.ingredients,
                ) { ingredient ->
                    ingredientsItem(
                        text = ingredient,
                        onCheckClick = {}
                    )
                }
            }
            Button(
                onClick = {}
            ) {
                Text(
                    text = stringResource(R.string.start_cooking_button)
                )
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