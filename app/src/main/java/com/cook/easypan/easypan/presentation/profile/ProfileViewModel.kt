package com.cook.easypan.easypan.presentation.profile

import ProfileAction
import ProfileState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel() : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

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