package com.cook.easypan.easypan.presentation.onboarding

import androidx.compose.runtime.Stable

@Stable
data class OnboardingState(
    val currentPage: Int = 0,
    val pages: List<OnboardingPage> = OnboardingPage.entries,
) {
    val pageCount: Int get() = pages.size
    val isLastPage: Boolean get() = currentPage == pageCount - 1
    val isNotificationPage: Boolean
        get() = pages.getOrNull(currentPage)?.isNotificationPrompt == true
}
