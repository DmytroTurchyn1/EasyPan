package com.cook.easypan.easypan.presentation.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.easypan.data.auth.GoogleAuthUiClient
import com.cook.easypan.easypan.domain.AuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthenticationViewModel(
    private val googleAuthUiClient: GoogleAuthUiClient,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthenticationState())
    val state = _state.asStateFlow()


    fun resetState() {
        _state.update { AuthenticationState() }
    }

    fun onAction(action: AuthenticationAction) {
        when (action) {
            AuthenticationAction.OnAuthButtonClick -> {
                viewModelScope.launch {
                    googleAuthUiClient.signIn().collect { response ->
                        when (response) {
                            is AuthResponse.Success -> {
                                _state.update {
                                    it.copy(
                                        isSignInSuccessful = true,
                                        signInError = null,
                                        currentUser = googleAuthUiClient.getSignedInUserWithData()
                                    )
                                }
                            }

                            is AuthResponse.Failure -> {
                                _state.update {
                                    it.copy(
                                        isSignInSuccessful = false,
                                        signInError = response.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}