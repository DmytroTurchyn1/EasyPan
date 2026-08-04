package com.cook.easypan.easypan.presentation.authentication

import com.cook.easypan.core.domain.AppError

data class AuthenticationState(
    val isSignInSuccessful: Boolean = false,
    val signInError: AppError? = null,
    val isLoading: Boolean = false,
)
