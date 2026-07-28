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

        var offer: ChefOffer? = ChefOffer(priceFormatted = "$4.99")
        var purchaseOutcome: PurchaseOutcome = PurchaseOutcome.Purchased
        var restoreResult: Result = Result.Success
        var purchaseGate: CompletableDeferred<Unit>? = null
        var purchaseCalls = 0

        override fun startObserving() = Unit
        override suspend fun getMonthlyOffer(): ChefOffer? = offer
        override suspend fun purchaseChef(activityContext: Context): PurchaseOutcome {
            purchaseCalls++
            purchaseGate?.await()
            return purchaseOutcome
        }

        override suspend fun restorePurchases(): Result = restoreResult
        override suspend fun logIn(userId: String) = Unit
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
    fun `offer loads into state on init`() = runTest {
        val viewModel = PaywallViewModel(billing)

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("$4.99", viewModel.state.value.priceFormatted)
        assertTrue(viewModel.state.value.canPurchase)
    }

    @Test
    fun `missing offer disables purchase`() = runTest {
        billing.offer = null
        val viewModel = PaywallViewModel(billing)

        assertNull(viewModel.state.value.priceFormatted)
        assertFalse(viewModel.state.value.canPurchase)
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
}
