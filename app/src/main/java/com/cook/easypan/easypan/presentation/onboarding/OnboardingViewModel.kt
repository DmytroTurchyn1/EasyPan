package com.cook.easypan.easypan.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cook.easypan.core.domain.NotificationPermissionChecker
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
    private val notificationPermission: NotificationPermissionChecker,
) : ViewModel() {

    private val initialState = OnboardingState(pages = visiblePages())

    private val _state = MutableStateFlow(initialState)
    val state = _state
        .asStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = initialState
        )

    private val _events = Channel<OnboardingEvent>()
    val events = _events.receiveAsFlow()

    private var lastTrackedPage: Int? = null
    private var isCompleting = false

    fun onAction(action: OnboardingAction) {
        when (action) {
            is OnboardingAction.OnPageChanged -> {
                val page = action.page.coerceIn(0, _state.value.pages.lastIndex)
                _state.update { it.copy(currentPage = page) }
                trackPage(page)
            }

            OnboardingAction.OnContinueClick -> {
                if (_state.value.isNotificationPage) {
                    viewModelScope.launch {
                        _events.send(OnboardingEvent.RequestNotificationPermission)
                    }
                } else {
                    advance()
                }
            }

            OnboardingAction.OnBackClick -> {
                _state.update { it.copy(currentPage = (it.currentPage - 1).coerceAtLeast(0)) }
            }

            OnboardingAction.OnSkipClick -> complete(AnalyticsValue.ONBOARDING_SKIPPED)

            OnboardingAction.OnNotNowClick -> {
                trackPermissionResult(AnalyticsValue.PERMISSION_DISMISSED)
                advance()
            }

            is OnboardingAction.OnNotificationPermissionResult -> {
                if (action.isGranted) {
                    trackPermissionResult(AnalyticsValue.PERMISSION_GRANTED)
                    notificationPermission.subscribeToTopicIfGranted()
                } else {
                    trackPermissionResult(AnalyticsValue.PERMISSION_DENIED)
                }
                advance()
            }
        }
    }

    private fun visiblePages(): List<OnboardingPage> {
        val showPrompt =
            notificationPermission.isRuntimePermissionRequired && !notificationPermission.isGranted()
        return OnboardingPage.entries.filter { !it.isNotificationPrompt || showPrompt }
    }

    private fun advance() {
        if (_state.value.isLastPage) {
            complete(AnalyticsValue.ONBOARDING_FINISHED)
        } else {
            _state.update { it.copy(currentPage = it.currentPage + 1) }
        }
    }

    private fun trackPage(page: Int) {
        if (lastTrackedPage == page) return
        lastTrackedPage = page
        val pages = _state.value.pages
        val screen = pages.getOrNull(page) ?: return
        analytics.track(
            AnalyticsEvent.SCREEN_VIEW,
            mapOf(AnalyticsParam.SCREEN to screen.analyticsScreen)
        )
    }

    private fun trackPermissionResult(result: String) {
        analytics.track(
            AnalyticsEvent.NOTIFICATION_PERMISSION,
            mapOf(AnalyticsParam.METHOD to result)
        )
    }

    private fun complete(method: String) {
        if (isCompleting) return
        isCompleting = true
        val current = _state.value
        analytics.track(
            AnalyticsEvent.ONBOARDING_COMPLETED,
            mapOf(
                AnalyticsParam.METHOD to method,
                AnalyticsParam.SCREEN to
                        current.pages[current.currentPage].analyticsScreen,
            )
        )
        viewModelScope.launch {
            userRepository.setOnboardingCompleted()
            _events.send(OnboardingEvent.Finish)
        }
    }
}
