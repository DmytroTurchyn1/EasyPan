/*
 * Created  18/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.authentication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.core.domain.Result
import com.cook.easypan.easypan.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthenticationViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthenticationState())
    val state = _state.asStateFlow()


    fun resetState() {
        _state.update { AuthenticationState() }
    }

    fun onAction(action: AuthenticationAction) {
        when (action) {
            is AuthenticationAction.OnAuthButtonClick -> signIn(action.activityContext)
        }
    }

    private fun signIn(activityContext: Context) {
        if (_state.value.isLoading) return
        _state.update {
            it.copy(
                isLoading = true,
                signInError = null
            )
        }
        viewModelScope.launch {
            when (val result = userRepository.signInWithGoogle(activityContext)) {
                // Published straight away so navigation happens the moment auth succeeds. Loading
                // the user profile here as well would hold the screen on a spinner through a second
                // network round-trip, and leaving that screen would cancel it half-done — the
                // Profile tab fetches the user itself anyway.
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isSignInSuccessful = true,
                            signInError = null,
                            isLoading = false
                        )
                    }
                }

                is Result.Failure -> {
                    _state.update {
                        it.copy(
                            isSignInSuccessful = false,
                            // A user-initiated cancellation is not an error to show.
                            signInError = result.error.takeUnless { error ->
                                error == AppError.SIGN_IN_CANCELLED
                            },
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
}