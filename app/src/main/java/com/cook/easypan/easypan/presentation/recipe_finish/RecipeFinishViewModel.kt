package com.cook.easypan.easypan.presentation.recipe_finish

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.easypan.domain.UserData
import com.cook.easypan.easypan.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class RecipeFinishViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(RecipeFinishState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                updateCookedRecipes()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = RecipeFinishState()
        )

    private suspend fun updateCookedRecipes() {
        try {
            val user = userRepository.getCurrentUser()
            userRepository.updateUserData(
                userId = user?.userId ?: throw IllegalStateException("User not signed in"),
                userData = UserData(
                    recipesCooked = (user.data?.recipesCooked ?: 0) + 1,
                )
            )
        } catch (e: Exception) {
            Log.e("Recipe Finish Screen", "Error updating user data: ${e.message}")
        }


    }

    fun onAction(action: RecipeFinishAction) {
        when (action) {
            else -> Unit
        }
    }

}