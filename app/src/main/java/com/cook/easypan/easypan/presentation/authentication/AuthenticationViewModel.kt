package com.cook.easypan.easypan.presentation.authentication

import android.content.Intent
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
) : ViewModel() {

    private val _state = MutableStateFlow(AuthenticationState())
    val state = _state.asStateFlow()

    /**
     * Updates the authentication state based on the provided sign-in result.
     *
     * Sets the sign-in success flag if user data is present, records any error message, and clears the sign-in intent sender.
     *
     * @param result The result of the sign-in attempt.
     */
    fun onLoginResult(result: SignInResult) {
        _state.update {
            it.copy(
                isSignInSuccessful = result.data != null,
                signInError = result.errorMessage,
                signInIntentSender = null
            )
        }

    }

    /**
     * Resets the authentication state to its default values.
     */
    fun resetState() {
        _state.update { AuthenticationState() }
    }

    /**
     * Handles authentication-related actions and updates the authentication state accordingly.
     *
     * When the authentication button is clicked, initiates the Google sign-in flow and updates the state with the resulting intent sender.
     *
     * @param action The authentication action to process.
     */
    fun onAction(action: AuthenticationAction) {
        when (action) {
            AuthenticationAction.OnAuthButtonClick -> {
                viewModelScope.launch {
                    val signInIntentSender = googleAuthUiClient.signIn()
                    _state.update {
                        it.copy(signInIntentSender = signInIntentSender)
                    }
                }
            }
        }
    }

    /**
     * Processes the result of a Google sign-in intent and updates the authentication state.
     *
     * If the provided intent data is not null, initiates the sign-in process using the Google authentication client
     * and updates the state based on the outcome.
     *
     * @param googleAuthUiClient The client used to handle Google sign-in operations.
     * @param data The intent containing the sign-in result, or null if unavailable.
     */
    fun handleSignInResult(googleAuthUiClient: GoogleAuthUiClient, data: Intent?) {
        viewModelScope.launch {
            if (data != null) {
                val signInResult = googleAuthUiClient.signInWithIntent(data)
                onLoginResult(signInResult)
            }
        }
    }
}