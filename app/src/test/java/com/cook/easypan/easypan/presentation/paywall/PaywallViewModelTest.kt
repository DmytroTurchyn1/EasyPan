/*
 * Created  27/7/2026
 *
 * Copyright (c) 2026 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.paywall

import android.content.Context
import android.util.Log
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.core.domain.Result
import com.cook.easypan.easypan.domain.model.ChefOffer
import com.cook.easypan.easypan.domain.model.ChefPlan
import com.cook.easypan.easypan.domain.model.PurchaseOutcome
import com.cook.easypan.easypan.domain.repository.BillingRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PaywallViewModelTest {

    private class FakeBillingRepository : BillingRepository {
        override val isChef = MutableStateFlow(false)

        var offers: List<ChefOffer> = listOf(YEARLY_OFFER, MONTHLY_OFFER)
        var purchaseOutcome: PurchaseOutcome = PurchaseOutcome.Purchased
        var restoreResult: Result = Result.Success
        var purchaseGate: CompletableDeferred<Unit>? = null
        var purchaseCalls = 0
        var purchasedPlan: ChefPlan? = null

        override fun startObserving() = Unit
        override suspend fun getChefOffers(): List<ChefOffer> = offers
        override suspend fun purchaseChef(
            activityContext: Context,
            plan: ChefPlan,
        ): PurchaseOutcome {
            purchaseCalls++
            purchasedPlan = plan
            purchaseGate?.await()
            return purchaseOutcome
        }

        override suspend fun restorePurchases(): Result = restoreResult
        override suspend fun logIn(userId: String) = Unit
        override fun setUserAttributes(email: String?, displayName: String?) = Unit
        override suspend fun logOut() = Unit
    }

    private lateinit var billing: FakeBillingRepository
    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        billing = FakeBillingRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `offers load into state on init`() = runTest {
        val viewModel = PaywallViewModel(billing)

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(listOf(YEARLY_OFFER, MONTHLY_OFFER), viewModel.state.value.offers)
        assertTrue(viewModel.state.value.canPurchase)
    }

    @Test
    fun `yearly is preselected when both plans are offered`() = runTest {
        val viewModel = PaywallViewModel(billing)

        assertEquals(ChefPlan.YEARLY, viewModel.state.value.selectedPlan)
        assertEquals(YEARLY_OFFER, viewModel.state.value.selectedOffer)
    }

    @Test
    fun `monthly is selected when the yearly plan is missing`() = runTest {
        billing.offers = listOf(MONTHLY_OFFER)
        val viewModel = PaywallViewModel(billing)

        assertEquals(ChefPlan.MONTHLY, viewModel.state.value.selectedPlan)
        assertTrue(viewModel.state.value.canPurchase)
    }

    @Test
    fun `missing offers disables purchase`() = runTest {
        billing.offers = emptyList()
        val viewModel = PaywallViewModel(billing)

        assertNull(viewModel.state.value.selectedPlan)
        assertNull(viewModel.state.value.selectedOffer)
        assertFalse(viewModel.state.value.canPurchase)
    }

    @Test
    fun `selecting monthly purchases the monthly plan`() = runTest {
        val viewModel = PaywallViewModel(billing)

        viewModel.onAction(PaywallAction.OnPlanSelect(ChefPlan.MONTHLY))
        assertEquals(ChefPlan.MONTHLY, viewModel.state.value.selectedPlan)

        viewModel.onAction(PaywallAction.OnPurchaseClick(context))

        assertEquals(ChefPlan.MONTHLY, billing.purchasedPlan)
    }

    @Test
    fun `default purchase bills the yearly plan`() = runTest {
        val viewModel = PaywallViewModel(billing)

        viewModel.onAction(PaywallAction.OnPurchaseClick(context))

        assertEquals(ChefPlan.YEARLY, billing.purchasedPlan)
    }

    @Test
    fun `plan selection is ignored while a purchase is in flight`() = runTest {
        billing.purchaseGate = CompletableDeferred()
        val viewModel = PaywallViewModel(billing)

        viewModel.onAction(PaywallAction.OnPurchaseClick(context))
        assertTrue(viewModel.state.value.isPurchasing)
        viewModel.onAction(PaywallAction.OnPlanSelect(ChefPlan.MONTHLY))

        assertEquals(ChefPlan.YEARLY, viewModel.state.value.selectedPlan)
        billing.purchaseGate?.complete(Unit)
    }

    @Test
    fun `successful purchase clears purchasing flag without error`() = runTest {
        val viewModel = PaywallViewModel(billing)

        viewModel.onAction(PaywallAction.OnPurchaseClick(context))

        assertFalse(viewModel.state.value.isPurchasing)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `cancelled purchase is silent`() = runTest {
        billing.purchaseOutcome = PurchaseOutcome.Cancelled
        val viewModel = PaywallViewModel(billing)

        viewModel.onAction(PaywallAction.OnPurchaseClick(context))

        assertFalse(viewModel.state.value.isPurchasing)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `failed purchase sets error`() = runTest {
        billing.purchaseOutcome = PurchaseOutcome.Failed(AppError.NETWORK)
        val viewModel = PaywallViewModel(billing)

        viewModel.onAction(PaywallAction.OnPurchaseClick(context))

        assertFalse(viewModel.state.value.isPurchasing)
        assertEquals(AppError.NETWORK, viewModel.state.value.error)
    }

    @Test
    fun `second purchase click while in flight is ignored`() = runTest {
        billing.purchaseGate = CompletableDeferred()
        val viewModel = PaywallViewModel(billing)

        viewModel.onAction(PaywallAction.OnPurchaseClick(context))
        assertTrue(viewModel.state.value.isPurchasing)
        viewModel.onAction(PaywallAction.OnPurchaseClick(context))

        billing.purchaseGate?.complete(Unit)
        assertEquals(1, billing.purchaseCalls)
        assertFalse(viewModel.state.value.isPurchasing)
    }

    @Test
    fun `restore success sets isRestored`() = runTest {
        val viewModel = PaywallViewModel(billing)

        viewModel.onAction(PaywallAction.OnRestoreClick)

        assertTrue(viewModel.state.value.isRestored)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `restore failure sets error`() = runTest {
        billing.restoreResult = Result.Failure(AppError.NETWORK)
        val viewModel = PaywallViewModel(billing)

        viewModel.onAction(PaywallAction.OnRestoreClick)

        assertFalse(viewModel.state.value.isRestored)
        assertEquals(AppError.NETWORK, viewModel.state.value.error)
    }

    private companion object {
        val YEARLY_OFFER = ChefOffer(
            plan = ChefPlan.YEARLY,
            priceFormatted = "$59.99",
            pricePerMonthFormatted = "$5.00",
            freeTrialDays = 7,
            savingsPercent = 50
        )
        val MONTHLY_OFFER = ChefOffer(
            plan = ChefPlan.MONTHLY,
            priceFormatted = "$9.99",
            freeTrialDays = 7
        )
    }
}
