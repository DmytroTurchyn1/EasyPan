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
import com.cook.easypan.easypan.domain.model.ChefPlan
import com.cook.easypan.easypan.domain.model.PurchaseOutcome
import com.cook.easypan.easypan.domain.repository.BillingRepository
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offering
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
import com.revenuecat.purchases.models.Period
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class DefaultBillingRepository(
    private val analytics: FirebaseAnalytics
) : BillingRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _isChef = MutableStateFlow(false)
    override val isChef = _isChef.asStateFlow()

    /**
     * Packages resolved by the last [getChefOffers] call, so a purchase bills the exact package
     * whose price the user was shown without a second offerings round-trip.
     */
    @Volatile
    private var cachedPackages: Map<ChefPlan, Package> = emptyMap()

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

    override suspend fun getChefOffers(): List<ChefOffer> {
        val offering = currentOffering() ?: return emptyList()
        val packages = buildMap {
            offering.packageFor(ChefPlan.MONTHLY)?.let { put(ChefPlan.MONTHLY, it) }
            offering.packageFor(ChefPlan.YEARLY)?.let { put(ChefPlan.YEARLY, it) }
        }
        cachedPackages = packages

        val monthly = packages[ChefPlan.MONTHLY]
        val yearly = packages[ChefPlan.YEARLY]
        // Yearly first — it is the plan the paywall preselects.
        return listOfNotNull(
            yearly?.toOffer(ChefPlan.YEARLY, savingsPercent(monthly = monthly, yearly = yearly)),
            monthly?.toOffer(ChefPlan.MONTHLY, savingsPercent = null),
        )
    }

    override suspend fun purchaseChef(activityContext: Context, plan: ChefPlan): PurchaseOutcome {
        if (!Purchases.isConfigured) return PurchaseOutcome.Failed(AppError.UNKNOWN)
        val activity = activityContext as? Activity
            ?: return PurchaseOutcome.Failed(AppError.UNKNOWN)
        val pkg = cachedPackages[plan]
            ?: currentOffering()?.packageFor(plan)
            ?: return PurchaseOutcome.Failed(AppError.NETWORK)
        return try {
            val result = Purchases.sharedInstance.awaitPurchase(
                PurchaseParams.Builder(activity, pkg).build()
            )
            analytics.logEvent(FirebaseAnalytics.Event.PURCHASE) {
                param(FirebaseAnalytics.Param.VALUE, pkg.product.price.amountMicros / 1_000_000.0)
                param(FirebaseAnalytics.Param.CURRENCY, pkg.product.price.currencyCode)
                param(FirebaseAnalytics.Param.TRANSACTION_ID, result.storeTransaction.orderId ?: "")
            }
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
            analytics.logEvent("purchases_restored") {
                param("purchases_restored", info.entitlements.active.toString())
            }
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

    override fun setUserAttributes(email: String?, displayName: String?) {
        if (!Purchases.isConfigured) return
        try {
            // Both are local cache writes keyed by the current appUserID; the SDK uploads them on
            // its next backend round-trip, so this never blocks the caller.
            Purchases.sharedInstance.setEmail(email)
            Purchases.sharedInstance.setDisplayName(displayName)
        } catch (e: Exception) {
            // Never let an attribute write break sign-in or deletion.
            Log.e(TAG, "Failed to set customer attributes", e)
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

    private suspend fun currentOffering(): Offering? {
        if (!Purchases.isConfigured) return null
        return try {
            Purchases.sharedInstance.awaitOfferings().current
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch offerings", e)
            null
        }
    }

    private fun Offering.packageFor(plan: ChefPlan): Package? = when (plan) {
        // The typed accessors only match the predefined $rc_monthly / $rc_annual identifiers, so
        // fall back to the billing period for offerings built with custom package identifiers.
        ChefPlan.MONTHLY -> monthly ?: availablePackages.firstOrNull {
            val months = it.product.period?.valueInMonths ?: return@firstOrNull false
            months >= 1.0 && months < 2.0
        }

        ChefPlan.YEARLY -> annual ?: availablePackages.firstOrNull {
            // Also catches a yearly base plan configured as P12M rather than P1Y.
            (it.product.period?.valueInMonths ?: 0.0) >= 12.0
        }
    }

    private fun Package.toOffer(plan: ChefPlan, savingsPercent: Int?) = ChefOffer(
        plan = plan,
        priceFormatted = product.price.formatted,
        // Only adds information for plans billed less often than monthly.
        pricePerMonthFormatted = product.formattedPricePerMonth()
            .takeIf { plan == ChefPlan.YEARLY },
        // defaultOption is the option awaitPurchase will buy, so the advertised trial matches what
        // is actually purchased. Play decides eligibility at purchase time — this says the offer
        // exists, not that this user still qualifies for it.
        freeTrialDays = product.defaultOption?.freePhase?.billingPeriod?.inDays(),
        savingsPercent = savingsPercent,
    )

    /** Period length in whole days, or null when it is zero or the unit is unrecognized. */
    private fun Period.inDays(): Int? {
        // Period.valueInDays is @InternalRevenueCatAPI, so convert off the stable value/unit pair.
        val days = when (unit) {
            Period.Unit.DAY -> value
            Period.Unit.WEEK -> value * DAYS_PER_WEEK
            Period.Unit.MONTH -> value * DAYS_PER_MONTH
            Period.Unit.YEAR -> value * DAYS_PER_YEAR
            Period.Unit.UNKNOWN -> 0
        }
        return days.takeIf { it > 0 }
    }

    private fun savingsPercent(monthly: Package?, yearly: Package?): Int? {
        val monthlyPrice = monthly?.product?.price ?: return null
        val yearlyPerMonth = yearly?.product?.pricePerMonth() ?: return null
        // Comparing across currencies would produce a nonsense percentage.
        if (monthlyPrice.currencyCode != yearlyPerMonth.currencyCode) return null
        if (monthlyPrice.amountMicros <= 0L) return null
        val saved = (1.0 - yearlyPerMonth.amountMicros.toDouble() / monthlyPrice.amountMicros) * 100
        return saved.roundToInt().takeIf { it > 0 }
    }

    private fun CustomerInfo.isChefActive(): Boolean =
        entitlements.active[ENTITLEMENT_CHEF] != null

    private companion object {
        const val TAG = "BillingRepository"
        const val DAYS_PER_WEEK = 7
        const val DAYS_PER_MONTH = 30
        const val DAYS_PER_YEAR = 365
    }
}
