package com.cook.easypan.easypan.presentation.recipe_finish

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.easypan.data.auth.GoogleAuthUiClient
import com.cook.easypan.easypan.domain.UserData
import com.cook.easypan.easypan.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class RecipeFinishViewModel(
    private val userRepository: UserRepository,
    private val googleAuthUiClient: GoogleAuthUiClient
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
            val cookedRecipes =
                userRepository.getUserData("TxiGwY9sPde8V6g3YJnipjLy53o1").recipesCooked
            val user = googleAuthUiClient.getSignedInUserWithData()
            Log.d("FirestoreClient", "cooked recipes updating user data userRepo: $cookedRecipes")
            userRepository.updateUserData(
                userId = user?.userId ?: "111",
                userData = UserData(
                    recipesCooked = (user?.data?.recipesCooked ?: 0) + 1,
                )
            )
        } catch (e: Exception) {
            Log.e("FirestoreClient", "Error updating user data screen: ${e.message}")
        }


    }

    fun onAction(action: RecipeFinishAction) {
        when (action) {
            else -> Unit
        }
    }

}