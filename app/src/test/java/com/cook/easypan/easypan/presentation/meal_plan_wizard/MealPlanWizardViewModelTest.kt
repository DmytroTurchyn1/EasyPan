package com.cook.easypan.easypan.presentation.meal_plan_wizard

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class MealPlanWizardViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `continue advances the step and progress`() = runTest {
        val viewModel = MealPlanWizardViewModel()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        assertEquals(1, viewModel.state.value.step)
        assertEquals(0.2f, viewModel.state.value.progressBar, 0.0001f)

        viewModel.onAction(MealPlanWizardAction.OnContinueClick)

        assertEquals(2, viewModel.state.value.step)
        assertEquals(0.4f, viewModel.state.value.progressBar, 0.0001f)
        collectJob.cancel()
    }

    @Test
    fun `selecting a portion enables continue on step 2`() = runTest {
        val viewModel = MealPlanWizardViewModel()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        viewModel.onAction(MealPlanWizardAction.OnContinueClick) // -> step 2
        assertFalse(viewModel.state.value.canContinue)

        viewModel.onAction(MealPlanWizardAction.OnPortionsSelect(3))

        assertEquals(3, viewModel.state.value.portions)
        assertTrue(viewModel.state.value.canContinue)
        collectJob.cancel()
    }

    @Test
    fun `checking an allergy adds then removes it`() = runTest {
        val viewModel = MealPlanWizardViewModel()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        viewModel.onAction(MealPlanWizardAction.OnAllergyCheck("Peanuts"))
        assertTrue("Peanuts" in viewModel.state.value.selectedAllergies)

        viewModel.onAction(MealPlanWizardAction.OnAllergyCheck("Peanuts"))
        assertFalse("Peanuts" in viewModel.state.value.selectedAllergies)
        collectJob.cancel()
    }

    @Test
    fun `skip on the allergies step clears selection and advances`() = runTest {
        val viewModel = MealPlanWizardViewModel()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        // Advance to step 3 and select an allergy.
        viewModel.onAction(MealPlanWizardAction.OnContinueClick) // -> step 2
        viewModel.onAction(MealPlanWizardAction.OnContinueClick) // -> step 3
        viewModel.onAction(MealPlanWizardAction.OnAllergyCheck("Soy"))
        assertEquals(3, viewModel.state.value.step)
        assertTrue("Soy" in viewModel.state.value.selectedAllergies)

        viewModel.onAction(MealPlanWizardAction.OnSkipClick)

        assertEquals(4, viewModel.state.value.step)
        assertTrue(viewModel.state.value.selectedAllergies.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun `back decrements the step`() = runTest {
        val viewModel = MealPlanWizardViewModel()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        viewModel.onAction(MealPlanWizardAction.OnContinueClick) // -> step 2
        assertEquals(2, viewModel.state.value.step)

        viewModel.onAction(MealPlanWizardAction.OnBackClick)

        assertEquals(1, viewModel.state.value.step)
        collectJob.cancel()
    }

    @Test
    fun `back on the first step emits Exit`() = runTest {
        val viewModel = MealPlanWizardViewModel()
        val stateJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        val events = mutableListOf<MealPlanWizardEvent>()
        val eventsJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onAction(MealPlanWizardAction.OnBackClick)

        assertEquals(listOf<MealPlanWizardEvent>(MealPlanWizardEvent.Exit), events)
        stateJob.cancel()
        eventsJob.cancel()
    }

    @Test
    fun `continue on the last step emits Finish`() = runTest {
        val viewModel = MealPlanWizardViewModel()
        val stateJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        val events = mutableListOf<MealPlanWizardEvent>()
        val eventsJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }

        // Advance to the last step (1 -> 5).
        repeat(viewModel.state.value.steps - 1) {
            viewModel.onAction(MealPlanWizardAction.OnContinueClick)
        }
        assertTrue(viewModel.state.value.isLastStep)
        assertTrue(events.isEmpty())

        viewModel.onAction(MealPlanWizardAction.OnContinueClick)

        assertEquals(1, events.size)
        assertTrue(events.first() is MealPlanWizardEvent.Finish)
        stateJob.cancel()
        eventsJob.cancel()
    }

    @Test
    fun `cancel emits Exit`() = runTest {
        val viewModel = MealPlanWizardViewModel()
        val stateJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        val events = mutableListOf<MealPlanWizardEvent>()
        val eventsJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onAction(MealPlanWizardAction.OnCancelClick)

        assertEquals(listOf<MealPlanWizardEvent>(MealPlanWizardEvent.Exit), events)
        stateJob.cancel()
        eventsJob.cancel()
    }
}
