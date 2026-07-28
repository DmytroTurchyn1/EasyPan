/*
 * Created  27/7/2026
 *
 * Copyright (c) 2026 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.domain.model

import com.cook.easypan.core.domain.AppError

/**
 * The subscription offer shown on the paywall.
 *
 * @param priceFormatted localized store price, e.g. "$4.99".
 */
data class ChefOffer(
    val priceFormatted: String,
)

/** Result of a purchase attempt. A user-cancelled purchase is not an error. */
sealed interface PurchaseOutcome {
    data object Purchased : PurchaseOutcome
    data object Cancelled : PurchaseOutcome
    data class Failed(val error: AppError) : PurchaseOutcome
}
