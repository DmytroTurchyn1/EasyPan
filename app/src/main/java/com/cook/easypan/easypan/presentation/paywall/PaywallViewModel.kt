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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.core.domain.Result
import com.cook.easypan.easypan.domain.model.PurchaseOutcome
import com.cook.easypan.easypan.domain.repository.BillingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaywallViewModel(
    private val billingRepository: BillingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PaywallState())
    val state = _state.asStateFlow()

    init {
        loadOffer()
    }

    fun onAction(action: PaywallAction) {
        when (action) {
            is PaywallAction.OnPurchaseClick -> purchase(action.activityContext)
            is PaywallAction.OnRestoreClick -> restore()
        }
    }

    private fun loadOffer() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val offer = billingRepository.getMonthlyOffer()
                _state.update {
                    it.copy(isLoading = false, priceFormatted = offer?.priceFormatted)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load offer", e)
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun purchase(activityContext: Context) {
        if (!_state.value.canPurchase) return
        viewModelScope.launch {
            _state.update { it.copy(isPurchasing = true, error = null) }
            try {
                when (val outcome = billingRepository.purchaseChef(activityContext)) {
                    // Success needs no handling here: the entitlement listener flips
                    // BillingRepository.isChef and the gate swaps this screen out.
                    is PurchaseOutcome.Purchased, is PurchaseOutcome.Cancelled ->
                        _state.update { it.copy(isPurchasing = false) }

                    is PurchaseOutcome.Failed ->
                        _state.update { it.copy(isPurchasing = false, error = outcome.error) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Purchase crashed", e)
                _state.update { it.copy(isPurchasing = false, error = AppError.UNKNOWN) }
            }
        }
    }

    private fun restore() {
        if (_state.value.isPurchasing) return
        viewModelScope.launch {
            _state.update { it.copy(error = null, isRestored = false) }
            when (val result = billingRepository.restorePurchases()) {
                is Result.Success -> _state.update { it.copy(isRestored = true) }
                is Result.Failure -> _state.update { it.copy(error = result.error) }
            }
        }
    }

    private companion object {
        const val TAG = "PaywallVM"
    }
}
