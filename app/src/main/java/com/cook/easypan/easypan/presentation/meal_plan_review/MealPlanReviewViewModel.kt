package com.cook.easypan.easypan.presentation.meal_plan_review

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.easypan.domain.model.MealPlan
import com.cook.easypan.easypan.domain.model.MealPlanPreferences
import com.cook.easypan.easypan.domain.usecase.GenerateMealPlanUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class MealPlanReviewViewModel(
    private val generateMealPlan: GenerateMealPlanUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(MealPlanReviewState())
    val state = _state.asStateFlow()

    private val _events = Channel<MealPlanReviewEvent>()
    val events = _events.receiveAsFlow()

    // Kept so Retry can rebuild the same plan without re-plumbing the preferences.
    private var lastPreferences: MealPlanPreferences? = null

    fun onAction(action: MealPlanReviewAction) {
        when (action) {
            is MealPlanReviewAction.OnGenerate -> generate(action.preferences)
            MealPlanReviewAction.OnRetry -> lastPreferences?.let { generate(it) }
            is MealPlanReviewAction.OnRecipeClick ->
                sendEvent(MealPlanReviewEvent.OpenRecipe(action.recipe))

            MealPlanReviewAction.OnContinueClick,
            MealPlanReviewAction.OnDismissPlanClick -> sendEvent(MealPlanReviewEvent.Dismiss)

            MealPlanReviewAction.OnEditClick -> sendEvent(MealPlanReviewEvent.EditPlan)
        }
    }

    private fun generate(preferences: MealPlanPreferences) {
        lastPreferences = preferences
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val plan = generateMealPlan(preferences)
                _state.update { plan.toReviewState() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate meal plan", e)
                _state.update { it.copy(isLoading = false, error = AppError.UNKNOWN) }
            }
        }
    }

    private fun MealPlan.toReviewState(): MealPlanReviewState {
        val locale = Locale.getDefault()
        val formatter = DateTimeFormatter.ofPattern("MMM d", locale)
        val weekRange = days.takeIf { it.isNotEmpty() }?.let { planDays ->
            val start = planDays.first().date
            val end = planDays.last().date
            val endLabel = if (start.month == end.month) {
                end.dayOfMonth.toString()
            } else {
                end.format(formatter)
            }
            "${start.format(formatter)} – $endLabel"
        }.orEmpty()

        return MealPlanReviewState(
            isLoading = false,
            error = null,
            weekRange = weekRange,
            people = preferences.people,
            mealsDay = preferences.mealsDay,
            allergies = preferences.allergies,
            days = days.map { day ->
                PlanDayUi(
                    dayName = day.date.dayOfWeek.getDisplayName(TextStyle.FULL, locale),
                    dateLabel = day.date.format(formatter),
                    meals = day.meals,
                )
            },
        )
    }

    private fun sendEvent(event: MealPlanReviewEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private companion object {
        const val TAG = "MealPlanReviewVM"
    }
}
