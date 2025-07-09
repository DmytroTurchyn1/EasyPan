package com.dmytro_turchyn.easypan.easypan.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
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
import com.dmytro_turchyn.easypan.R
import com.dmytro_turchyn.easypan.easypan.presentation.home.components.RecipeComplexity
import com.dmytro_turchyn.easypan.easypan.presentation.home.components.RecipeList
import com.dmytro_turchyn.easypan.easypan.presentation.home.components.RecipeListItem
import com.dmytro_turchyn.easypan.ui.theme.EasyPanTheme
import org.koin.androidx.compose.koinViewModel


@Composable
fun HomeRoot(
    viewModel: HomeViewModel = koinViewModel<HomeViewModel>()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

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
            ){
                Text(
                    text = stringResource(R.string.home_title),
                    modifier = Modifier
                        .padding(bottom = 10.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

        }
    ) { innerPAdding ->
        RecipeList(
            modifier = Modifier.padding(innerPAdding),
            recipes = state.recipes,
            onRecipeClick = { recipe ->
                onAction(HomeAction.OnRecipeClick(recipe))
            },
            scrollState = recipeListState
        )
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