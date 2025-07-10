package com.cook.easypan.easypan.presentation.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.easypan.data.auth.GoogleAuthUiClient
import com.cook.easypan.easypan.domain.SignInResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthenticationViewModel(
    private val googleAuthUiClient: GoogleAuthUiClient,
): ViewModel() {

    private val _state = MutableStateFlow(AuthenticationState())
    val state = _state.asStateFlow()

    fun onLoginResult(result: SignInResult) {
        _state.update { it.copy(
            isSignInSuccessful =  result.data != null,
            signInError = result.errorMessage,
            signInIntentSender = null
        ) }

    }

    fun resetState() {
        _state.update { AuthenticationState() }
    }
    fun onAction(action: AuthenticationAction) {
        when (action) {
            AuthenticationAction.OnAuthButtonClick -> {
                viewModelScope.launch {
                    val signInIntentSender = googleAuthUiClient.signIn()
                    _state.update {
                        it.copy(signInIntentSender = signInIntentSender)
                    }
                    println("button clicked, intent sender: $signInIntentSender")
                }
            }
        }
    }

}