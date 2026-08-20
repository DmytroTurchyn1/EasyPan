package com.cook.easypan.easypan.presentation.onboarding

sealed interface OnboardingAction {
    data class OnPageChanged(val page: Int) : OnboardingAction
    data object OnContinueClick : OnboardingAction
    data object OnSkipClick : OnboardingAction
    data object OnBackClick : OnboardingAction
}
