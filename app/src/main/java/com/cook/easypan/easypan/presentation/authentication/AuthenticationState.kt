package com.cook.easypan.easypan.presentation.authentication

data class AuthenticationState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null,
)