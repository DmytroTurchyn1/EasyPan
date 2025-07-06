package com.dmytro_turchyn.easypan.easypan.presentation.profile

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dmytro_turchyn.easypan.R
import com.dmytro_turchyn.easypan.ui.theme.EasyPanTheme

@Composable
fun ProfileRoot(
    viewModel: ProfileViewModel = viewModel<ProfileViewModel>()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProfileScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
private fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
) {
    Text(
        text = "Profile",
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
        ProfileScreen(
            state = ProfileState(),
            onAction = {}
        )
    }
}