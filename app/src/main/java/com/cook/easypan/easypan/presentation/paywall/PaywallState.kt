/*
 * Created  27/7/2026
 *
 * Copyright (c) 2026 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.paywall

import com.cook.easypan.core.domain.AppError
import com.cook.easypan.easypan.domain.model.ChefOffer
import com.cook.easypan.easypan.domain.model.ChefPlan

data class PaywallState(
    val isLoading: Boolean = true,
    val offers: List<ChefOffer> = emptyList(),
    val selectedPlan: ChefPlan? = null,
    val isPurchasing: Boolean = false,
    val isRestored: Boolean = false,
    val error: AppError? = null,
) {
    val selectedOffer: ChefOffer? get() = offers.firstOrNull { it.plan == selectedPlan }
    val canPurchase: Boolean get() = selectedOffer != null && !isPurchasing
}
