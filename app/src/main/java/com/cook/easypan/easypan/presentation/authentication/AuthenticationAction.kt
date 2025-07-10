package com.cook.easypan.easypan.presentation.authentication

sealed interface AuthenticationAction {
    object OnAuthButtonClick : AuthenticationAction
}