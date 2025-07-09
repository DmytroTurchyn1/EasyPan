package com.dmytro_turchyn.easypan.easypan.presentation.home

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmytro_turchyn.easypan.easypan.data.database.FirestoreClient
import com.dmytro_turchyn.easypan.easypan.domain.RecipeRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
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

    private fun loadRecipes(){
        viewModelScope.launch {
            _state.update {
                it.copy(
                    recipes = recipeRepository.getRecipes(),
                    isLoading = false
                )
            }
        }
    }
    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.OnRecipeClick ->{

            }
        }
    }

}