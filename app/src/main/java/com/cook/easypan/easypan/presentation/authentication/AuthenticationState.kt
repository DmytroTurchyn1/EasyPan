package com.cook.easypan.easypan.presentation.authentication

import android.content.IntentSender

data class AuthenticationState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null,
    val signInIntentSender : IntentSender? = null
)