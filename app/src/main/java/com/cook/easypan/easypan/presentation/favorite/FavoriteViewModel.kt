/*
 * Created  13/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.favorite

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.easypan.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(FavoriteState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadRecipes()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = FavoriteState()
        )

    private fun loadRecipes() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val favorites = userRepository.getFavoriteRecipes()
                _state.update {
                    it.copy(
                        favoriteRecipes = favorites,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Failed to load favorite recipes", e)
                val error =
                    if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                        AppError.NETWORK
                    } else {
                        AppError.UNKNOWN
                    }
                _state.update { it.copy(isLoading = false, error = error) }
            }
        }
    }

    fun onAction(action: FavoriteAction) {
        when (action) {
            is FavoriteAction.OnRetryClick -> loadRecipes()
            else -> Unit
        }
    }

}
