package com.cook.easypan.easypan.presentation.favorite

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cook.easypan.R
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun FavoriteRoot(
    viewModel: FavoriteViewModel = viewModel<FavoriteViewModel>()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    FavoriteScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
private fun FavoriteScreen(
    state: FavoriteState,
    onAction: (FavoriteAction) -> Unit,
) {
    Scaffold(
        topBar = {
            Box (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = stringResource(R.string.favorite_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp
                )
            }
        }
    ){ innerPadding ->
        if (state.favoriteRecipes.isEmpty()){
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = stringResource(R.string.no_favorite_recipes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
        }else{
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                items(state.favoriteRecipes.size){ index ->
                    TODO("Implement favorite recipe item view here")
                }
            }
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