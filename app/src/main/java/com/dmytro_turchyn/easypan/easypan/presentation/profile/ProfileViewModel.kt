package com.dmytro_turchyn.easypan.easypan.presentation.profile

import ProfileAction
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ProfileViewModel : ViewModel() {

    private val _state = MutableSharedFlow<ProfileAction>()
    val state: SharedFlow<ProfileAction> = _state.asSharedFlow()

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.OnSignOut -> {
                _state.tryEmit(ProfileAction.OnSignOut)
            }
        }
    }
}
