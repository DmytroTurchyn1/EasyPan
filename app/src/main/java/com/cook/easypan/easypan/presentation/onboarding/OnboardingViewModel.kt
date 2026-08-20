package com.cook.easypan.easypan.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.core.util.AnalyticsEvent
import com.cook.easypan.core.util.AnalyticsParam
import com.cook.easypan.core.util.AnalyticsValue
import com.cook.easypan.easypan.data.analytics.AnalyticsClient
import com.cook.easypan.easypan.domain.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val userRepository: UserRepository,
    private val analytics: AnalyticsClient,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state
        .asStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = OnboardingState()
        )

    private val _events = Channel<OnboardingEvent>()
    val events = _events.receiveAsFlow()

    private var lastTrackedPage: Int? = null
    private var isCompleting = false

    fun onAction(action: OnboardingAction) {
        when (action) {
            is OnboardingAction.OnPageChanged -> {
                val page = action.page.coerceIn(0, OnboardingPage.entries.lastIndex)
                _state.update { it.copy(currentPage = page) }
                trackPage(page)
            }

            OnboardingAction.OnContinueClick -> {
                if (_state.value.isLastPage) {
                    complete(AnalyticsValue.ONBOARDING_FINISHED)
                } else {
                    _state.update { it.copy(currentPage = it.currentPage + 1) }
                }
            }

            OnboardingAction.OnBackClick -> {
                _state.update { it.copy(currentPage = (it.currentPage - 1).coerceAtLeast(0)) }
            }

            OnboardingAction.OnSkipClick -> complete(AnalyticsValue.ONBOARDING_SKIPPED)
        }
    }

    private fun trackPage(page: Int) {
        if (lastTrackedPage == page) return
        lastTrackedPage = page
        analytics.track(
            AnalyticsEvent.SCREEN_VIEW,
            mapOf(AnalyticsParam.SCREEN to OnboardingPage.entries[page].analyticsScreen)
        )
    }

    private fun complete(method: String) {
        if (isCompleting) return
        isCompleting = true
        analytics.track(
            AnalyticsEvent.ONBOARDING_COMPLETED,
            mapOf(
                AnalyticsParam.METHOD to method,
                AnalyticsParam.SCREEN to
                        OnboardingPage.entries[_state.value.currentPage].analyticsScreen,
            )
        )
        viewModelScope.launch {
            userRepository.setOnboardingCompleted()
            _events.send(OnboardingEvent.Finish)
        }
    }
}
