/*
 * Created  27/7/2026
 *
 * Copyright (c) 2026 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.core.domain.Result
import com.cook.easypan.core.util.ENTITLEMENT_CHEF
import com.cook.easypan.easypan.domain.model.ChefOffer
import com.cook.easypan.easypan.domain.model.PurchaseOutcome
import com.cook.easypan.easypan.domain.repository.BillingRepository
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesErrorCode
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitLogIn
import com.revenuecat.purchases.awaitLogOut
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DefaultBillingRepository : BillingRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _isChef = MutableStateFlow(false)
    override val isChef = _isChef.asStateFlow()

    override fun startObserving() {
        if (!Purchases.isConfigured) {
            // Blank API key dev build: billing is inactive, so don't gate anyone out.
            Log.e(TAG, "Billing not configured — premium features are left unlocked")
            _isChef.value = true
            return
        }
        // Single source of truth: purchases, restores, renewals, and expirations all land here.
        Purchases.sharedInstance.updatedCustomerInfoListener =
            UpdatedCustomerInfoListener { info ->
                _isChef.value = info.isChefActive()
            }
        // Seed once so the first emission does not wait for a customer info update.
        scope.launch {
            try {
                _isChef.value = Purchases.sharedInstance.awaitCustomerInfo().isChefActive()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to seed entitlement state", e)
            }
        }
    }

    override suspend fun getMonthlyOffer(): ChefOffer? {
        val pkg = monthlyPackage() ?: return null
        return ChefOffer(priceFormatted = pkg.product.price.formatted)
    }

    override suspend fun purchaseChef(activityContext: Context): PurchaseOutcome {
        if (!Purchases.isConfigured) return PurchaseOutcome.Failed(AppError.UNKNOWN)
        val activity = activityContext as? Activity
            ?: return PurchaseOutcome.Failed(AppError.UNKNOWN)
        val pkg = monthlyPackage()
            ?: return PurchaseOutcome.Failed(AppError.NETWORK)
        return try {
            Purchases.sharedInstance.awaitPurchase(
                PurchaseParams.Builder(activity, pkg).build()
            )
            // Content is not unlocked here — updatedCustomerInfoListener flips isChef.
            PurchaseOutcome.Purchased
        } catch (e: CancellationException) {
            throw e
        } catch (e: PurchasesException) {
            when (e.code) {
                PurchasesErrorCode.PurchaseCancelledError -> PurchaseOutcome.Cancelled
                PurchasesErrorCode.NetworkError -> PurchaseOutcome.Failed(AppError.NETWORK)
                else -> {
                    Log.e(TAG, "Purchase failed", e)
                    PurchaseOutcome.Failed(AppError.UNKNOWN)
                }
            }
        }
    }

    override suspend fun restorePurchases(): Result {
        if (!Purchases.isConfigured) return Result.Failure(AppError.UNKNOWN)
        return try {
            val info = Purchases.sharedInstance.awaitRestore()
            _isChef.value = info.isChefActive()
            Result.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: PurchasesException) {
            Log.e(TAG, "Restore failed", e)
            val error = if (e.code == PurchasesErrorCode.NetworkError) {
                AppError.NETWORK
            } else {
                AppError.UNKNOWN
            }
            Result.Failure(error)
        }
    }

    override suspend fun logIn(userId: String) {
        if (!Purchases.isConfigured) return
        try {
            val result = Purchases.sharedInstance.awaitLogIn(userId)
            _isChef.value = result.customerInfo.isChefActive()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Never block the sign-in flow on billing.
            Log.e(TAG, "Billing logIn failed", e)
        }
    }

    override suspend fun logOut() {
        if (!Purchases.isConfigured) return
        // logOut on an anonymous user throws LogOutWithAnonymousUserError.
        if (Purchases.sharedInstance.isAnonymous) return
        try {
            val info = Purchases.sharedInstance.awaitLogOut()
            _isChef.value = info.isChefActive()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Billing logOut failed", e)
        }
    }

    private suspend fun monthlyPackage(): Package? {
        if (!Purchases.isConfigured) return null
        return try {
            val current = Purchases.sharedInstance.awaitOfferings().current
            current?.monthly ?: current?.availablePackages?.firstOrNull()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch offerings", e)
            null
        }
    }

    private fun CustomerInfo.isChefActive(): Boolean =
        entitlements.active[ENTITLEMENT_CHEF] != null

    private companion object {
        const val TAG = "BillingRepository"
    }
}
