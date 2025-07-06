package com.dmytro_turchyn.easypan.easypan.presentation.favorite

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dmytro_turchyn.easypan.ui.theme.EasyPanTheme

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
    Text(
        text = "Favorite",
        modifier = Modifier
            .padding(bottom = 10.dp),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Preview
@Composable
private fun Preview() {
    EasyPanTheme {
        FavoriteScreen(
            state = FavoriteState(),
            onAction = {}
        )
    }
}