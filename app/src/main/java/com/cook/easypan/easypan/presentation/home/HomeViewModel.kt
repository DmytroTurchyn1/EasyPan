/*
 * Created  14/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

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
import com.cook.easypan.easypan.domain.model.Recipe
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
    private var allRecipes: List<Recipe> = emptyList()


    private val _state = MutableStateFlow(HomeState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadData()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HomeState()
        )

    private fun loadData() {
        viewModelScope.launch {
            val recipes = recipeRepository.getRecipes()
            allRecipes = recipes
            val filters = recipes.map { recipe ->
                recipe.chips
            }.flatten().distinct().sorted()
            _state.update {
                it.copy(
                    recipes = recipes,
                    filterList = filters,
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
            is HomeAction.OnFilterSelected -> {
                val newFilter =
                    if (state.value.selectedFilter == action.filter) "" else action.filter
                val newRecipeList = if (newFilter.isEmpty()) {
                    allRecipes
                } else {
                    allRecipes.filter { it.chips.contains(newFilter) }
                }
                _state.update { st ->
                    st.copy(
                        selectedFilter = newFilter,
                        recipes = newRecipeList
                    )
                }
            }
        }
    }

}