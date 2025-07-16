package com.cook.easypan.easypan.presentation.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class FavoriteViewModel : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(FavoriteState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                /** Load initial data here **/
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = FavoriteState()
        )

    /**
     * Handles actions related to the favorite feature.
     *
     * @param action The action to be processed.
     * @throws NotImplementedError Always thrown as action handling is not yet implemented.
     */
    fun onAction(action: FavoriteAction) {
        when (action) {
            else -> TODO("Handle actions")
        }
    }

}