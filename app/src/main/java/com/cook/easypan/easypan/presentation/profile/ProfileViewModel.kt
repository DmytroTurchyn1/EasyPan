/*
 * Created  14/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.profile

import ProfileAction
import ProfileState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.easypan.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {
    private var hasLoadedInitialData = false
    private val _state = MutableStateFlow(ProfileState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadUserData()
                getKeepScreenOn()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProfileState()
        )

    private fun loadUserData() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            _state.update {
                it.copy(
                    currentUser = user,
                    isLoading = false
                )
            }
        }
    }

    private fun getKeepScreenOn() {
        viewModelScope.launch {
            userRepository.getKeepScreenOnDataStore().collectLatest { toggleState ->
                _state.update { currentState ->
                    currentState.copy(
                        keepScreenOn = toggleState
                    )
                }
            }
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
            ProfileAction.OnKeepScreenOnToggle -> {

                viewModelScope.launch {
                    userRepository.updateKeepScreenOnDataStore(!state.value.keepScreenOn)
                }
                _state.update { currentState ->
                    currentState.copy(
                        keepScreenOn = !currentState.keepScreenOn
                    )
                }
            }
            else -> Unit
        }
    }
}