/*
 * Created  25/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.recipe_step

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.core.CountdownTimer
import com.cook.easypan.easypan.domain.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecipeStepViewModel(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_STEP = "recipe_step_index"
    }

    private var hasLoadedInitialData = false


    private val _state = MutableStateFlow(
        // Survives process death: the step index is restored while the recipe
        // itself is re-delivered through OnRecipeChange.
        RecipeStepState(step = savedStateHandle[KEY_STEP] ?: 0)
    )


    private val _events = Channel<RecipeStepEvent>()
    val events = _events.receiveAsFlow()

    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                getKeepScreenOn()
                observeTimer()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = RecipeStepState()
        )

    private fun observeTimer() {
        viewModelScope.launch {
            combine(
                CountdownTimer.remainingSeconds,
                CountdownTimer.ownerId,
                CountdownTimer.isRunning
            ) { remaining, owner, running ->
                Triple(remaining, owner, running)
            }.collect { (remaining, owner, running) ->
                _state.update {
                    it.copy(
                        timerRemainingSeconds = remaining,
                        timerOwnerStep = owner,
                        timerRunning = running
                    )
                }
            }
        }
    }

    private fun getKeepScreenOn() {
        viewModelScope.launch {
            userRepository.getKeepScreenOnDataStore().collectLatest { toggleState ->
                _state.update { currentState ->
                    currentState.copy(
                        keepScreenOn = toggleState
                    )
                }
            }
        }
    }

    fun onAction(action: RecipeStepAction) {
        when (action) {
            is RecipeStepAction.OnNextClick -> updateStep(_state.value.step + 1)

            is RecipeStepAction.OnPreviousClick -> {
                if (_state.value.step > 0) {
                    updateStep(_state.value.step - 1)
                }
            }

            is RecipeStepAction.OnRecipeChange -> {
                _state.update {
                    val stepCount = action.recipe.instructions.size
                    val safeStep = it.step.coerceIn(0, (stepCount - 1).coerceAtLeast(0))
                    it.copy(
                        recipe = action.recipe,
                        isLoading = false,
                        step = safeStep,
                        progressBar = progressFor(safeStep, stepCount)
                    )
                }
            }

            is RecipeStepAction.OnDismissDialog -> {
                _state.update {
                    it.copy(
                        isDialogShowing = false
                    )
                }
            }

            is RecipeStepAction.OnShowDialog -> {
                _state.update {
                    it.copy(
                        isDialogShowing = true
                    )
                }
            }

            is RecipeStepAction.OnCancelClick -> {
                // Leaving the cooking flow; the timer must not outlive it.
                CountdownTimer.stop()
                _state.update {
                    it.copy(
                        isDialogShowing = false
                    )
                }
            }

            is RecipeStepAction.OnFinishClick -> {
                CountdownTimer.stop()
                _state.update {
                    it.copy(
                        isFinishButtonEnabled = false
                    )
                }
            }

            is RecipeStepAction.OnTimerToggleClick ->
                toggleTimer(action.stepIndex, action.totalSeconds)

            is RecipeStepAction.OnTimerRestartClick -> restartTimer(action.stepIndex)

        }
    }

    private fun toggleTimer(stepIndex: Int, totalSeconds: Int) {
        val current = _state.value
        val isMine = current.timerOwnerStep == stepIndex
        viewModelScope.launch {
            if (current.timerRunning && isMine) {
                _events.send(RecipeStepEvent.PauseTimer)
            } else {
                // Resume this step's paused countdown, otherwise start fresh —
                // replacing another step's running timer.
                val startFromSeconds = (if (isMine) current.timerRemainingSeconds else null)
                    ?.takeIf { it in 1 until totalSeconds.toLong() }
                    ?: totalSeconds.toLong()
                _events.send(
                    RecipeStepEvent.StartTimer(
                        durationMs = startFromSeconds * 1000L,
                        ownerStep = stepIndex
                    )
                )
            }
        }
    }

    private fun restartTimer(stepIndex: Int) {
        if (_state.value.timerOwnerStep != stepIndex) return
        // Reset the in-process timer synchronously; the service stop intent is
        // delivered asynchronously and would leave the old value on screen.
        CountdownTimer.stop()
        viewModelScope.launch {
            _events.send(RecipeStepEvent.StopTimer)
        }
    }

    private fun updateStep(newStep: Int) {
        val stepCount = _state.value.recipe?.instructions?.size ?: return
        if (stepCount <= 0) return
        val safeStep = newStep.coerceIn(0, stepCount - 1)
        savedStateHandle[KEY_STEP] = safeStep
        _state.update {
            it.copy(
                step = safeStep,
                progressBar = progressFor(safeStep, stepCount)
            )
        }
    }

    // Progress is derived from the step index instead of accumulated, so it
    // cannot drift from floating-point addition.
    private fun progressFor(step: Int, stepCount: Int): Float =
        if (stepCount <= 0) 0f else (step + 1).toFloat() / stepCount.toFloat()
}
