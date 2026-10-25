package com.cook.easypan.easypan.presentation.onboarding

import com.cook.easypan.core.domain.NotificationPermissionChecker
import com.cook.easypan.core.util.AnalyticsEvent
import com.cook.easypan.easypan.data.analytics.AnalyticsClient
import com.cook.easypan.easypan.domain.repository.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private class FakeNotificationPermissionChecker(
        override val isRuntimePermissionRequired: Boolean = true,
        private var granted: Boolean = false,
    ) : NotificationPermissionChecker {
        var subscribeCalls = 0

        override fun isGranted(): Boolean = granted

        override fun subscribeToTopicIfGranted() {
            subscribeCalls++
        }
    }

    private val testDispatcher = UnconfinedTestDispatcher()

    @RelaxedMockK
    private lateinit var userRepository: UserRepository

    @RelaxedMockK
    private lateinit var analytics: AnalyticsClient

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `includes the notification page when the permission is still missing`() {
        val viewModel = viewModel(FakeNotificationPermissionChecker(granted = false))

        assertTrue(viewModel.state.value.pages.contains(OnboardingPage.NOTIFICATIONS))
        assertEquals(OnboardingPage.entries.size, viewModel.state.value.pageCount)
    }

    @Test
    fun `drops the notification page when the permission is already granted`() {
        val viewModel = viewModel(FakeNotificationPermissionChecker(granted = true))

        assertFalse(viewModel.state.value.pages.contains(OnboardingPage.NOTIFICATIONS))
        assertEquals(OnboardingPage.entries.size - 1, viewModel.state.value.pageCount)
    }

    @Test
    fun `drops the notification page when there is no runtime permission`() {
        val viewModel = viewModel(
            FakeNotificationPermissionChecker(isRuntimePermissionRequired = false, granted = false)
        )

        assertFalse(viewModel.state.value.pages.contains(OnboardingPage.NOTIFICATIONS))
    }

    @Test
    fun `continue on the notification page asks for the permission instead of advancing`() =
        runTest(testDispatcher) {
            val viewModel = viewModel(FakeNotificationPermissionChecker())
            val (events, eventsJob) = collectEvents(viewModel)
            val stateJob = subscribe(viewModel)
            goToNotificationPage(viewModel)

            viewModel.onAction(OnboardingAction.OnContinueClick)

            assertEquals(listOf(OnboardingEvent.RequestNotificationPermission), events)
            assertTrue(viewModel.state.value.isNotificationPage)
            eventsJob.cancel()
            stateJob.cancel()
        }

    @Test
    fun `a granted permission subscribes to the topic and advances`() = runTest(testDispatcher) {
        val permission = FakeNotificationPermissionChecker()
        val viewModel = viewModel(permission)
        val stateJob = subscribe(viewModel)
        val notificationPage = goToNotificationPage(viewModel)

        viewModel.onAction(OnboardingAction.OnNotificationPermissionResult(isGranted = true))

        assertEquals(1, permission.subscribeCalls)
        assertEquals(notificationPage + 1, viewModel.state.value.currentPage)
        verify { analytics.track(AnalyticsEvent.NOTIFICATION_PERMISSION, any()) }
        stateJob.cancel()
    }

    @Test
    fun `a denied permission advances without subscribing`() = runTest(testDispatcher) {
        val permission = FakeNotificationPermissionChecker()
        val viewModel = viewModel(permission)
        val stateJob = subscribe(viewModel)
        val notificationPage = goToNotificationPage(viewModel)

        viewModel.onAction(OnboardingAction.OnNotificationPermissionResult(isGranted = false))

        assertEquals(0, permission.subscribeCalls)
        assertEquals(notificationPage + 1, viewModel.state.value.currentPage)
        stateJob.cancel()
    }

    @Test
    fun `not now advances without asking for the permission`() = runTest(testDispatcher) {
        val permission = FakeNotificationPermissionChecker()
        val viewModel = viewModel(permission)
        val (events, eventsJob) = collectEvents(viewModel)
        val stateJob = subscribe(viewModel)
        val notificationPage = goToNotificationPage(viewModel)

        viewModel.onAction(OnboardingAction.OnNotNowClick)

        assertTrue(events.isEmpty())
        assertEquals(0, permission.subscribeCalls)
        assertEquals(notificationPage + 1, viewModel.state.value.currentPage)
        eventsJob.cancel()
        stateJob.cancel()
    }

    @Test
    fun `continue on the last page completes the onboarding`() = runTest(testDispatcher) {
        val viewModel = viewModel(FakeNotificationPermissionChecker())
        val (events, eventsJob) = collectEvents(viewModel)
        viewModel.onAction(
            OnboardingAction.OnPageChanged(viewModel.state.value.pages.lastIndex)
        )

        viewModel.onAction(OnboardingAction.OnContinueClick)

        assertEquals(listOf(OnboardingEvent.Finish), events)
        coVerify { userRepository.setOnboardingCompleted() }
        eventsJob.cancel()
    }

    private fun goToNotificationPage(viewModel: OnboardingViewModel): Int {
        val index = viewModel.state.value.pages.indexOf(OnboardingPage.NOTIFICATIONS)
        viewModel.onAction(OnboardingAction.OnPageChanged(index))
        return index
    }

    private fun viewModel(permission: NotificationPermissionChecker) = OnboardingViewModel(
        userRepository = userRepository,
        analytics = analytics,
        notificationPermission = permission,
    )

    private fun CoroutineScope.subscribe(viewModel: OnboardingViewModel): Job =
        launch(testDispatcher) { viewModel.state.collect { } }

    private fun CoroutineScope.collectEvents(
        viewModel: OnboardingViewModel,
    ): Pair<List<OnboardingEvent>, Job> {
        val events = mutableListOf<OnboardingEvent>()
        val job = launch(testDispatcher) { viewModel.events.collect { events += it } }
        return events to job
    }
}
