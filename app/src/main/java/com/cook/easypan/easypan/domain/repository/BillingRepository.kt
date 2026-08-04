/*
 * Created  27/7/2026
 *
 * Copyright (c) 2026 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.domain.repository

import android.content.Context
import com.cook.easypan.core.domain.Result
import com.cook.easypan.easypan.domain.model.ChefOffer
import com.cook.easypan.easypan.domain.model.ChefPlan
import com.cook.easypan.easypan.domain.model.PurchaseOutcome
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {

    /** Whether the current user has the "chef" entitlement. Single source of truth for gating. */
    val isChef: StateFlow<Boolean>

    /**
     * Starts listening for entitlement changes and seeds [isChef] with the current state.
     * Call once at app startup, after the billing SDK is configured.
     */
    fun startObserving()

    /**
     * The selectable "chef" plans in display order (yearly first), or empty when offerings are
     * unavailable.
     */
    suspend fun getChefOffers(): List<ChefOffer>

    /**
     * Launches the store billing flow for the "chef" package matching [plan].
     * [activityContext] must be the currently visible Activity — the store attaches its sheet to it.
     */
    suspend fun purchaseChef(activityContext: Context, plan: ChefPlan): PurchaseOutcome

    /** Syncs the store receipt to re-grant entitlements after a reinstall or device switch. */
    suspend fun restorePurchases(): Result

    /** Ties billing identity to the signed-in user. Call after auth confirms the session. */
    suspend fun logIn(userId: String)

    /**
     * Attaches identifying attributes to the current billing customer, for dashboard and support
     * use. Null clears an attribute.
     *
     * Must be called *after* [logIn] — attributes bind to whichever customer is current when they
     * are set, so calling this first would tag the anonymous customer instead.
     */
    fun setUserAttributes(email: String?, displayName: String?)

    /** Returns billing identity to anonymous. Call on sign-out and account deletion. */
    suspend fun logOut()
}
