package com.cook.easypan.easypan.presentation.authentication

import android.content.Context

sealed interface AuthenticationAction {
    data class OnAuthButtonClick(val activityContext: Context) : AuthenticationAction
}