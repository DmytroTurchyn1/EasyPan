package com.cook.easypan.easypan.presentation.home

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.core.util.NOTIFICATION_TOPIC
import com.cook.easypan.easypan.domain.repository.RecipeRepository
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private var hasLoadedInitialData = false


    private val _state = MutableStateFlow(HomeState())
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
            initialValue = HomeState()
        )

    private fun loadRecipes() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    recipes = recipeRepository.getRecipes(),
                    isLoading = false
                )
            }
        }
    }


    fun checkNotificationPermission(context: Context): Boolean {
        return when {

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                checkSelfPermission(
                    context,
                    POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            }

            else -> {
                NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }
    }


    fun onPermissionResult(
        isGranted: Boolean
    ) {
        if (isGranted) {
            if (!_state.value.hasSubscribedToTopic) {
                Firebase.messaging.subscribeToTopic(NOTIFICATION_TOPIC)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _state.update { currentState ->
                                currentState.copy(
                                    hasSubscribedToTopic = true
                                )
                            }
                            Log.d("HomeViewModel", "Subscribed to topic successfully")
                        } else {
                            Log.e(
                                "HomeViewModel",
                                "Failed to subscribe to topic: ${task.exception?.message}"
                            )
                        }
                    }
            }
        } else {
            Log.e("HomeViewModel", "Notification permission denied")
        }
    }


    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.OnRecipeClick -> {

            }
        }
    }

}