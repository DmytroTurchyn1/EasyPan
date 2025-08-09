package com.cook.easypan.easypan.presentation.authentication

import com.cook.easypan.easypan.domain.model.User

data class AuthenticationState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null,
    val currentUser: User? = null,
    val isLoading: Boolean = false,
)