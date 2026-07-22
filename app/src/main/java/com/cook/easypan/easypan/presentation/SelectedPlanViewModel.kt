package com.cook.easypan.easypan.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.cook.easypan.easypan.domain.model.MealPlanPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class SelectedPlanViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _preferences = MutableStateFlow(restore())
    val preferences = _preferences.asStateFlow()

    fun onPlanRequested(preferences: MealPlanPreferences) {
        savedStateHandle[KEY_FAVORITE] = ArrayList(preferences.favoriteProducts)
        savedStateHandle[KEY_SKIP] = ArrayList(preferences.skipProducts)
        savedStateHandle[KEY_ALLERGIES] = ArrayList(preferences.allergies)
        savedStateHandle[KEY_MEALS_DAY] = preferences.mealsDay
        savedStateHandle[KEY_PEOPLE] = preferences.people
        _preferences.value = preferences
    }

    private fun restore(): MealPlanPreferences? {
        val mealsDay = savedStateHandle.get<Int>(KEY_MEALS_DAY) ?: return null
        val people = savedStateHandle.get<Int>(KEY_PEOPLE) ?: return null
        return MealPlanPreferences(
            favoriteProducts = savedStateHandle.get<ArrayList<String>>(KEY_FAVORITE).orEmpty(),
            skipProducts = savedStateHandle.get<ArrayList<String>>(KEY_SKIP).orEmpty(),
            allergies = savedStateHandle.get<ArrayList<String>>(KEY_ALLERGIES).orEmpty(),
            mealsDay = mealsDay,
            people = people,
        )
    }

    private companion object {
        const val KEY_FAVORITE = "plan_favorite"
        const val KEY_SKIP = "plan_skip"
        const val KEY_ALLERGIES = "plan_allergies"
        const val KEY_MEALS_DAY = "plan_meals_day"
        const val KEY_PEOPLE = "plan_people"
    }
}
