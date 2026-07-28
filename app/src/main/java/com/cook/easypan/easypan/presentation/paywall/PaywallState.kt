/*
 * Created  27/7/2026
 *
 * Copyright (c) 2026 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.paywall

import com.cook.easypan.core.domain.AppError

data class PaywallState(
    val isLoading: Boolean = true,
    val priceFormatted: String? = null,
    val isPurchasing: Boolean = false,
    val isRestored: Boolean = false,
    val error: AppError? = null,
) {
    val canPurchase: Boolean get() = priceFormatted != null && !isPurchasing
}
