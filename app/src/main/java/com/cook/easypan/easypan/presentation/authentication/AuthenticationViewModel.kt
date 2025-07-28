package com.cook.easypan.easypan.presentation.authentication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.easypan.data.auth.AuthResponse
import com.cook.easypan.easypan.data.auth.GoogleAuthUiClient
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
                        if (response is AuthResponse.Success) {
                            val userWithData = googleAuthUiClient.getSignedInUserWithData()
                            Log.d("Auth data", "User data: ${userWithData!!.data?.recipesCooked}")
                            _state.update {
                                it.copy(
                                    isSignInSuccessful = true,
                                    signInError = null,
                                    currentUser = userWithData
                                )
                            }
                        } else {
                            _state.update {
                                it.copy(
                                    isSignInSuccessful = false,
                                    signInError = (response as AuthResponse.Failure).error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}