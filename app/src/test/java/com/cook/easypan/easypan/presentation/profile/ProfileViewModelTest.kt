package com.cook.easypan.easypan.presentation.profile

import com.cook.easypan.easypan.domain.model.User
import com.cook.easypan.easypan.domain.model.UserData
import com.cook.easypan.easypan.domain.repository.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    // Shared with runTest below so the WhileSubscribed timeout can be advanced in virtual time.
    private val testDispatcher = UnconfinedTestDispatcher()

    @RelaxedMockK
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        every { userRepository.getKeepScreenOnDataStore() } returns flowOf(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `loads the user on first subscribe`() = runTest(testDispatcher) {
        coEvery { userRepository.getCurrentUser() } returns userWithData
        val viewModel = ProfileViewModel(userRepository)

        val subscription = subscribe(viewModel)

        assertEquals(3, viewModel.state.value.currentUser?.data?.recipesCooked)
        subscription.cancel()
    }

    @Test
    fun `a load without user data is retried on the next subscribe`() = runTest(testDispatcher) {
        // getCurrentUser falls back to the bare Firebase session when the Firestore fetch fails, so
        // the user is non-null but carries no data. That must not latch as "loaded", or the profile
        // stays blank for as long as the ViewModel lives.
        coEvery { userRepository.getCurrentUser() } returns User(userId = "u1")
        val viewModel = ProfileViewModel(userRepository)

        val first = subscribe(viewModel)
        assertNull(viewModel.state.value.currentUser?.data)
        first.cancel()
        advanceTimeBy(WHILE_SUBSCRIBED_TIMEOUT_MS + 1_000)

        coEvery { userRepository.getCurrentUser() } returns userWithData
        val second = subscribe(viewModel)

        coVerify(atLeast = 2) { userRepository.getCurrentUser() }
        assertEquals(3, viewModel.state.value.currentUser?.data?.recipesCooked)
        second.cancel()
    }

    @Test
    fun `a successful load is not refetched on resubscribe`() = runTest(testDispatcher) {
        coEvery { userRepository.getCurrentUser() } returns userWithData
        val viewModel = ProfileViewModel(userRepository)

        val first = subscribe(viewModel)
        first.cancel()
        advanceTimeBy(WHILE_SUBSCRIBED_TIMEOUT_MS + 1_000)
        val second = subscribe(viewModel)

        coVerify(exactly = 1) { userRepository.getCurrentUser() }
        second.cancel()
    }

    /** `stateIn` only runs `onStart` for a real collector — reading `.value` does not subscribe. */
    private fun CoroutineScope.subscribe(viewModel: ProfileViewModel): Job =
        launch(testDispatcher) { viewModel.state.collect { } }

    private val userWithData = User(userId = "u1", data = UserData(recipesCooked = 3))

    private companion object {
        const val WHILE_SUBSCRIBED_TIMEOUT_MS = 5_000L
    }
}
