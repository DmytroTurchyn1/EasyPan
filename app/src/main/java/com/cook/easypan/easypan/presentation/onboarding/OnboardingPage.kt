package com.cook.easypan.easypan.presentation.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.cook.easypan.R

enum class OnboardingVisual {
    HERO,
    STEP_CARD,
    NOTIFICATION_STACK,
    RECIPE_CARDS,
}

enum class OnboardingPage(
    val visual: OnboardingVisual,
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    @param:DrawableRes val image: Int? = null,
) {
    COOK_EASY(
        visual = OnboardingVisual.HERO,
        title = R.string.onboarding_screen_1_title,
        description = R.string.onboarding_screen_1_description,
        image = R.drawable.onboarding_hero_1
    ),
    ONE_STEP(
        visual = OnboardingVisual.STEP_CARD,
        title = R.string.onboarding_screen_2_title,
        description = R.string.onboarding_screen_2_description
    ),
    NOTIFICATIONS(
        visual = OnboardingVisual.NOTIFICATION_STACK,
        title = R.string.onboarding_notifications_title,
        description = R.string.onboarding_notifications_description
    ),
    WEEK_SORTED(
        visual = OnboardingVisual.RECIPE_CARDS,
        title = R.string.onboarding_screen_3_title,
        description = R.string.onboarding_screen_3_description
    );

    val isNotificationPrompt: Boolean get() = visual == OnboardingVisual.NOTIFICATION_STACK

    val analyticsScreen: String get() = "Onboarding${ordinal + 1}"
}
