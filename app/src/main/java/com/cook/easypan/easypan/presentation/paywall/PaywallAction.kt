/*
 * Created  27/7/2026
 *
 * Copyright (c) 2026 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.paywall

import android.content.Context

sealed interface PaywallAction {
    data class OnPurchaseClick(val activityContext: Context) : PaywallAction
    data object OnRestoreClick : PaywallAction
}
