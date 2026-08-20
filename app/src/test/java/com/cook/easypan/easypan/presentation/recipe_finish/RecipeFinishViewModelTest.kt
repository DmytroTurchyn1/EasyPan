package com.cook.easypan.easypan.presentation.recipe_finish

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.cook.easypan.core.util.AnalyticsEvent
import com.cook.easypan.easypan.data.analytics.AnalyticsClient
import com.cook.easypan.easypan.domain.model.User
import com.cook.easypan.easypan.domain.model.UserData
import com.cook.easypan.easypan.domain.repository.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkStatic
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class RecipeFinishViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @RelaxedMockK
    private lateinit var userRepository: UserRepository

    @RelaxedMockK
    private lateinit var analytics: AnalyticsClient

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `requests a review on the first cooked recipe`() = runTest(testDispatcher) {
        coEvery { userRepository.getCurrentUser() } returns User(
            userId = "u1",
            data = UserData(recipesCooked = 0),
        )
        val viewModel = viewModel()

        val stateJob = subscribe(viewModel)
        val (events, eventsJob) = collectEvents(viewModel)

        assertEquals(listOf(RecipeFinishEvent.RequestReview), events)
        verify { analytics.track(AnalyticsEvent.FIRST_RECIPE_FINISHED, any()) }
        eventsJob.cancel()
        stateJob.cancel()
    }

    @Test
    fun `does not request a review for a returning cook`() = runTest(testDispatcher) {
        coEvery { userRepository.getCurrentUser() } returns User(
            userId = "u1",
            data = UserData(recipesCooked = 7),
        )
        val viewModel = viewModel()

        val (events, eventsJob) = collectEvents(viewModel)
        val stateJob = subscribe(viewModel)

        assertTrue(events.isEmpty())
        verify { analytics.track(AnalyticsEvent.RECIPE_FINISHED, any()) }
        eventsJob.cancel()
        stateJob.cancel()
    }

    @Test
    fun `does not request a review when the user data failed to load`() = runTest(testDispatcher) {
        coEvery { userRepository.getCurrentUser() } returns User(userId = "u1")
        val viewModel = viewModel()

        val (events, eventsJob) = collectEvents(viewModel)
        val stateJob = subscribe(viewModel)

        assertTrue(events.isEmpty())
        verify(exactly = 0) { analytics.track(AnalyticsEvent.FIRST_RECIPE_FINISHED, any()) }
        eventsJob.cancel()
        stateJob.cancel()
    }

    @Test
    fun `does not request a review when there is no signed in user`() = runTest(testDispatcher) {
        coEvery { userRepository.getCurrentUser() } returns null
        val viewModel = viewModel()

        val (events, eventsJob) = collectEvents(viewModel)
        val stateJob = subscribe(viewModel)

        assertTrue(events.isEmpty())
        verify(exactly = 0) { analytics.track(AnalyticsEvent.FIRST_RECIPE_FINISHED, any()) }
        eventsJob.cancel()
        stateJob.cancel()
    }

    @Test
    fun `still reports the finished recipe count when the review is skipped`() =
        runTest(testDispatcher) {
            coEvery { userRepository.getCurrentUser() } returns User(userId = "u1")
            val viewModel = viewModel()

            val stateJob = subscribe(viewModel)

            assertEquals(1, viewModel.state.value.userFinishedRecipes)
            stateJob.cancel()
        }

    private fun viewModel() = RecipeFinishViewModel(
        userRepository = userRepository,
        savedStateHandle = SavedStateHandle(mapOf("id" to RECIPE_ID)),
        analytics = analytics,
    )

    private fun CoroutineScope.subscribe(viewModel: RecipeFinishViewModel): Job =
        launch(testDispatcher) { viewModel.state.collect { } }

    private fun CoroutineScope.collectEvents(
        viewModel: RecipeFinishViewModel,
    ): Pair<List<RecipeFinishEvent>, Job> {
        val events = mutableListOf<RecipeFinishEvent>()
        val job = launch(testDispatcher) { viewModel.events.collect { events.add(it) } }
        return events to job
    }

    private companion object {
        const val RECIPE_ID = "recipe-1"
    }
}
