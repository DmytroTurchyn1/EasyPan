package com.cook.easypan.easypan.presentation.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.cook.easypan.R

enum class OnboardingPage(
    @param:DrawableRes val image: Int,
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
) {
    COOK_EASY(
        image = R.drawable.onboarding_hero_1,
        title = R.string.onboarding_screen_1_title,
        description = R.string.onboarding_screen_1_description
    ),
    ONE_STEP(
        image = R.drawable.onboarding_hero_2,
        title = R.string.onboarding_screen_2_title,
        description = R.string.onboarding_screen_2_description
    ),
    TIMERS(
        image = R.drawable.onboarding_hero_2,
        title = R.string.onboarding_screen_3_title,
        description = R.string.onboarding_screen_3_description
    ),
    WEEK_SORTED(
        image = R.drawable.onboarding_hero_2,
        title = R.string.onboarding_screen_4_title,
        description = R.string.onboarding_screen_4_description
    ),
    READY(
        image = R.drawable.onboarding_hero_2,
        title = R.string.onboarding_screen_5_title,
        description = R.string.onboarding_screen_5_description
    );

    val analyticsScreen: String get() = "Onboarding${ordinal + 1}"
}
