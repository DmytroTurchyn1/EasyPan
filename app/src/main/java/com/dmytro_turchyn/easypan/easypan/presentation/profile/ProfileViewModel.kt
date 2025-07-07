package com.dmytro_turchyn.easypan.easypan.presentation.profile

import ProfileAction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _state = MutableSharedFlow<ProfileAction>()
    val state = _state.asSharedFlow()

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnSignOut -> {
                viewModelScope.launch {
                    _state.emit(ProfileAction.OnSignOut)
                }
            }
        }
    }
}