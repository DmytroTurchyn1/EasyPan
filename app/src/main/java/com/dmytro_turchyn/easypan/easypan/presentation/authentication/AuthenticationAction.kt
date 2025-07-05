package com.dmytro_turchyn.easypan.easypan.presentation.authentication

sealed interface AuthenticationAction {
    object OnAuthButtonClick : AuthenticationAction
}