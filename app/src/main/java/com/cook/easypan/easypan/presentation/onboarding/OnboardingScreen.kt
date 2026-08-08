package com.cook.easypan.easypan.presentation.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun OnboardingRoot(
    viewModel: OnboardingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    OnboardingScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun OnboardingScreen(
    state: OnboardingState,
    onAction: (OnboardingAction) -> Unit,
) {

}

@Preview
@Composable
private fun Preview() {
    EasyPanTheme {
        OnboardingScreen(
            state = OnboardingState(),
            onAction = {}
        )
    }
}