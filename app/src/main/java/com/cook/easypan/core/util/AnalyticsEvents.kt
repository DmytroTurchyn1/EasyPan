/*
 * Created  8/8/2026
 *
 * Copyright (c) 2026 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.core.util

object AnalyticsEvent {
    const val SIGN_UP = "sign_up"
    const val PURCHASE = "purchase"
    const val PURCHASES_RESTORED = "purchases_restored"
    const val FIRST_RECIPE_FINISHED = "first_recipe_finished"
    const val RECIPE_FINISHED = "recipe_finished"
    const val MEAL_PLAN_GENERATED = "meal_plan_generated"
    const val SCREEN_VIEW = "screen_view"
    const val ONBOARDING_COMPLETED = "onboarding_completed"
}

object AnalyticsParam {
    const val USER_ID = "user_id"
    const val RECIPE_ID = "recipe_id"
    const val VALUE = "value"
    const val CURRENCY = "currency"
    const val TRANSACTION_ID = "transaction_id"
    const val ENTITLEMENTS = "entitlements"
    const val SCREEN = "screen"
    const val METHOD = "method"
}

object AnalyticsValue {
    const val ONBOARDING_FINISHED = "finished"
    const val ONBOARDING_SKIPPED = "skipped"
}
