package com.cook.easypan.easypan.presentation.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.core.domain.AuthResponse
import com.cook.easypan.easypan.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthenticationViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthenticationState())
    val state = _state.asStateFlow()


    fun resetState() {
        _state.update { AuthenticationState() }
    }

    fun onAction(action: AuthenticationAction) {
        when (action) {
            is AuthenticationAction.OnAuthButtonClick -> {
                _state.update {
                    it.copy(
                        isLoading = true
                    )
                }
                viewModelScope.launch {
                    userRepository.signInWithGoogle(action.activityContext).collect { response ->
                        when (response) {
                            is AuthResponse.Success -> {
                                _state.update {
                                    it.copy(
                                        isSignInSuccessful = true,
                                        signInError = null,
                                        currentUser = userRepository.getCurrentUser(),
                                        isLoading = false
                                    )
                                }
                            }

                            is AuthResponse.Failure -> {
                                _state.update {
                                    it.copy(
                                        isSignInSuccessful = false,
                                        signInError = response.error,
                                        isLoading = false
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