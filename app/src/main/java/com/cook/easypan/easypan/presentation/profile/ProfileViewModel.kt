/*
 * Created  15/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.core.domain.Result
import com.cook.easypan.core.presentation.snackBar.SnackBarController
import com.cook.easypan.core.presentation.snackBar.SnackBarEvent
import com.cook.easypan.core.presentation.toMessageRes
import com.cook.easypan.easypan.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {
    private var hasLoadedInitialData = false
    private var hasLoadedUser = false
    private val _state = MutableStateFlow(ProfileState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                getKeepScreenOn()
                hasLoadedInitialData = true
            }
            // Deliberately outside the guard: loadUserData latches its own flag only once it has
            // real data, so a failed fetch retries when the tab is reopened instead of leaving the
            // profile permanently blank.
            loadUserData()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProfileState()
        )

    private fun loadUserData() {
        if (hasLoadedUser) return
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            // getCurrentUser falls back to the bare Firebase session when the data fetch fails, so
            // a non-null user is not proof of success — the profile stats live in `data`.
            hasLoadedUser = user?.data != null
            _state.update {
                it.copy(
                    currentUser = user,
                    isLoading = false
                )
            }
        }
    }

    private fun getKeepScreenOn() {
        viewModelScope.launch {
            try {
                userRepository.getKeepScreenOnDataStore()
                    .distinctUntilChanged()
                    .collectLatest { toggleState ->
                        _state.update { currentState ->
                            currentState.copy(
                                keepScreenOn = toggleState
                            )
                        }
                    }

            } catch (_: Throwable) {
                Log.e("ProfileViewModel", "Error getting keep screen on state")
            }
        }
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnSignOut -> {
                _state.update { currentState ->
                    currentState.copy(
                        isSignedOut = true
                    )
                }
            }

            ProfileAction.OnKeepScreenOnToggle -> {
                viewModelScope.launch {
                    val newState = !state.value.keepScreenOn
                    runCatching {
                        userRepository.updateKeepScreenOnDataStore(newState)
                    }.onSuccess { newState ->
                        _state.update {
                            it.copy(
                                keepScreenOn = newState
                            )
                        }
                    }.onFailure {
                        Log.e(
                            "ProfileViewModel",
                            "Error updating keep screen on state: ${it.message}"
                        )
                    }
                }
            }

            ProfileAction.OnDeleteAccountClick -> {
                _state.update { it.copy(isDeleteDialogShowing = true) }
            }

            ProfileAction.OnDeleteAccountDismiss -> {
                _state.update { it.copy(isDeleteDialogShowing = false) }
            }

            is ProfileAction.OnDeleteAccountConfirm -> deleteAccount(action.activityContext)

            else -> Unit
        }
    }

    private fun deleteAccount(activityContext: Context) {
        if (_state.value.isAccountDeleted) return
        _state.update {
            it.copy(
                isDeleteDialogShowing = false,
                isLoading = true
            )
        }
        viewModelScope.launch {
            when (val result = userRepository.deleteAccount(activityContext)) {
                is Result.Success -> {
                    _state.update { it.copy(isAccountDeleted = true) }
                }

                is Result.Failure -> {
                    _state.update { it.copy(isLoading = false) }
                    if (result.error != AppError.SIGN_IN_CANCELLED) {
                        SnackBarController.sendEvent(
                            SnackBarEvent(messageRes = result.error.toMessageRes())
                        )
                    }
                }
            }
        }
    }
}
