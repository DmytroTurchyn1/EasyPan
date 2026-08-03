/*
 * Created  27/7/2026
 *
 * Copyright (c) 2026 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.domain.model

import com.cook.easypan.core.domain.AppError

/** Billing cadence of a paywall offer. */
enum class ChefPlan { MONTHLY, YEARLY }

/**
 * One purchasable plan on the paywall.
 *
 * @param priceFormatted localized price for the whole billing period, e.g. "$59.99".
 * @param pricePerMonthFormatted that price normalized to a month, e.g. "$5.00". Null for plans
 *   already billed monthly, and when the store cannot derive it.
 * @param freeTrialDays length of the plan's free phase, or null when it has none.
 * @param savingsPercent whole-percent saving against the monthly plan. Only ever set on
 *   [ChefPlan.YEARLY], and only when both plans are priced in the same currency.
 */
data class ChefOffer(
    val plan: ChefPlan,
    val priceFormatted: String,
    val pricePerMonthFormatted: String? = null,
    val freeTrialDays: Int? = null,
    val savingsPercent: Int? = null,
)

/** Result of a purchase attempt. A user-cancelled purchase is not an error. */
sealed interface PurchaseOutcome {
    data object Purchased : PurchaseOutcome
    data object Cancelled : PurchaseOutcome
    data class Failed(val error: AppError) : PurchaseOutcome
}
