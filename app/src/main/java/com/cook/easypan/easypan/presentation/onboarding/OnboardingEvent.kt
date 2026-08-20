package com.cook.easypan.easypan.presentation.onboarding

sealed interface OnboardingEvent {
    data object Finish : OnboardingEvent
}
