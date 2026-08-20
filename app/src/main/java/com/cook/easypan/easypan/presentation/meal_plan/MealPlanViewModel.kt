package com.cook.easypan.easypan.presentation.meal_plan

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.easypan.domain.model.MealPlan
import com.cook.easypan.easypan.domain.model.MealPlanPreferences
import com.cook.easypan.easypan.domain.repository.BillingRepository
import com.cook.easypan.easypan.domain.repository.MealPlanRepository
import com.cook.easypan.easypan.domain.usecase.GenerateMealPlanUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class MealPlanViewModel(
    private val generateMealPlan: GenerateMealPlanUseCase,
    private val billingRepository: BillingRepository,
    private val mealPlanRepository: MealPlanRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MealPlanState())
    val state = _state.asStateFlow()

    // The screen restores the saved plan on init while the nav layer fires OnGenerate at almost the
    // same moment. Holding one job means the later request always wins instead of racing.
    private var planJob: Job? = null

    init {
        billingRepository.isChef
            .onEach { isChef -> _state.update { it.copy(isProUser = isChef) } }
            .launchIn(viewModelScope)

        // This ViewModel is scoped to the MealPlan back stack entry, so it is rebuilt on every tab
        // switch. The plan is read back from disk rather than regenerated.
        planJob = viewModelScope.launch { showSavedPlan() }
    }

    private val _events = Channel<MealPlanEvent>()
    val events = _events.receiveAsFlow()

    // Kept so Retry can rebuild the same plan without re-plumbing the preferences.
    private var lastPreferences: MealPlanPreferences? = null


    fun onAction(action: MealPlanAction) {
        when (action) {
            is MealPlanAction.OnGenerate -> generate(action.preferences)
            MealPlanAction.OnRetry -> lastPreferences?.let { generate(it) }
            is MealPlanAction.OnDaySelected ->
                _state.update { it.copy(selectedDayIndex = action.index) }

            is MealPlanAction.OnRecipeClick -> sendEvent(MealPlanEvent.OpenRecipe(action.recipe))

            MealPlanAction.OnRegenerateClick,
            MealPlanAction.OnEditClick,
            MealPlanAction.OnCreatePlanClick -> sendEvent(MealPlanEvent.OpenWizard)
        }
    }

    private fun generate(preferences: MealPlanPreferences) {
        lastPreferences = preferences
        planJob?.cancel()
        planJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Returning to this screen re-runs OnGenerate with the same preferences. Reuse the
                // stored plan instead of building a different one behind the user's back.
                val saved = mealPlanRepository.getSavedPlan()
                val plan = if (saved != null && saved.preferences == preferences) {
                    saved
                } else {
                    generateMealPlan(preferences).also { mealPlanRepository.savePlan(it) }
                }
                show(plan)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate meal plan", e)
                _state.update { it.copy(isLoading = false, error = AppError.UNKNOWN) }
            }
        }
    }

    private suspend fun showSavedPlan() {
        try {
            val saved = mealPlanRepository.getSavedPlan() ?: return
            lastPreferences = saved.preferences
            show(saved)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read the saved meal plan", e)
        }
    }

    private fun show(plan: MealPlan) {
        // toState() builds a fresh state — re-apply the entitlement flag.
        _state.update { plan.toState().copy(isProUser = billingRepository.isChef.value) }
    }

    private fun MealPlan.toState(): MealPlanState {
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

        return MealPlanState(
            isLoading = false,
            error = null,
            weekRange = weekRange,
            people = preferences.people,
            mealsDay = preferences.mealsDay,
            days = days.map { day ->
                MealPlanDayUi(
                    dayName = day.date.dayOfWeek.getDisplayName(TextStyle.FULL, locale),
                    dayAbbrev = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                    dayOfMonth = day.date.dayOfMonth,
                    meals = day.meals,
                )
            },
            selectedDayIndex = 0,
        )
    }

    private fun sendEvent(event: MealPlanEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private companion object {
        const val TAG = "MealPlanVM"
    }
}
