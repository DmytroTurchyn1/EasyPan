package com.cook.easypan.easypan.presentation.profile

import ProfileAction
import ProfileState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.easypan.data.auth.GoogleAuthUiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ProfileViewModel(
    private val googleAuthUiClient: GoogleAuthUiClient,
) : ViewModel() {
    private var hasLoadedInitialData = false
    private val _state = MutableStateFlow(ProfileState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadUserData()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProfileState()
        )

    private suspend fun loadUserData() {
        _state.update {
            it.copy(
                currentUser = googleAuthUiClient.getSignedInUserWithData(),
                isLoading = false
            )
        }
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnSignOut -> {
                _state.update { currentState ->
                    currentState.copy(
                        isSignedOut = true
                    )
                }
            }

            ProfileAction.OnNotificationsToggle -> {
                _state.update { currentState ->
                    currentState.copy(
                        notificationsEnabled = !currentState.notificationsEnabled
                    )
                }
            }
        }
    }
}