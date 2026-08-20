package com.cook.easypan.easypan.presentation.meal_plan

import android.content.Context
import android.util.Log
import com.cook.easypan.core.domain.Result
import com.cook.easypan.easypan.domain.model.ChefOffer
import com.cook.easypan.easypan.domain.model.ChefPlan
import com.cook.easypan.easypan.domain.model.Ingredient
import com.cook.easypan.easypan.domain.model.MealPlan
import com.cook.easypan.easypan.domain.model.MealPlanPreferences
import com.cook.easypan.easypan.domain.model.PlanDay
import com.cook.easypan.easypan.domain.model.PurchaseOutcome
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.domain.repository.BillingRepository
import com.cook.easypan.easypan.domain.repository.MealPlanRepository
import com.cook.easypan.easypan.domain.repository.RecipeRepository
import com.cook.easypan.easypan.domain.usecase.GenerateMealPlanUseCase
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MealPlanViewModelTest {

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `OnGenerate populates the week`() = runTest {
        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog()))

        viewModel.onAction(
            MealPlanAction.OnGenerate(MealPlanPreferences(mealsDay = 3, people = 2)),
        )

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(7, state.days.size)
        assertTrue(state.weekRange.isNotEmpty())
        assertEquals(2, state.people)
        assertEquals(3, state.mealsDay)
        assertEquals(0, state.selectedDayIndex)
        assertEquals(3, state.days.first().meals.size)
    }

    @Test
    fun `OnDaySelected updates the selected index`() = runTest {
        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog()))
        viewModel.onAction(MealPlanAction.OnGenerate(MealPlanPreferences()))

        viewModel.onAction(MealPlanAction.OnDaySelected(2))

        assertEquals(2, viewModel.state.value.selectedDayIndex)
    }

    @Test
    fun `failure exposes an error instead of crashing`() = runTest {
        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog(), failFirst = true))

        viewModel.onAction(MealPlanAction.OnGenerate(MealPlanPreferences()))

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.days.isEmpty())
    }

    @Test
    fun `OnRetry regenerates after a failure`() = runTest {
        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog(), failFirst = true))

        viewModel.onAction(MealPlanAction.OnGenerate(MealPlanPreferences()))
        assertNotNull(viewModel.state.value.error)

        viewModel.onAction(MealPlanAction.OnRetry)

        val state = viewModel.state.value
        assertNull(state.error)
        assertTrue(state.days.isNotEmpty())
    }

    @Test
    fun `recipe click emits OpenRecipe`() = runTest {
        val recipe = defaultCatalog().first()
        val viewModel = viewModel(FakeRecipeRepository(listOf(recipe)))
        val events = mutableListOf<MealPlanEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onAction(MealPlanAction.OnRecipeClick(recipe))

        assertEquals(MealPlanEvent.OpenRecipe(recipe), events.single())
    }

    @Test
    fun `edit, regenerate and create all open the wizard`() = runTest {
        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog()))
        val events = mutableListOf<MealPlanEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onAction(MealPlanAction.OnEditClick)
        viewModel.onAction(MealPlanAction.OnRegenerateClick)
        viewModel.onAction(MealPlanAction.OnCreatePlanClick)

        assertEquals(3, events.size)
        assertTrue(events.all { it == MealPlanEvent.OpenWizard })
    }

    @Test
    fun `a saved plan is restored on init without regenerating`() = runTest {
        val saved = savedPlan()
        val repository = FakeMealPlanRepository(saved)

        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog()), repository)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.hasPlan)
        assertEquals(saved.days.size, state.days.size)
        assertEquals(0, repository.savedCount)
    }

    @Test
    fun `OnGenerate reuses the saved plan when preferences match`() = runTest {
        val saved = savedPlan()
        val repository = FakeMealPlanRepository(saved)
        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog()), repository)

        viewModel.onAction(MealPlanAction.OnGenerate(saved.preferences))

        assertEquals(saved.days.size, viewModel.state.value.days.size)
        assertEquals(0, repository.savedCount)
    }

    @Test
    fun `OnGenerate rebuilds and stores the plan when preferences differ`() = runTest {
        val saved = savedPlan()
        val repository = FakeMealPlanRepository(saved)
        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog()), repository)

        viewModel.onAction(
            MealPlanAction.OnGenerate(saved.preferences.copy(mealsDay = 2, people = 4)),
        )

        assertEquals(1, repository.savedCount)
        assertEquals(4, viewModel.state.value.people)
        assertEquals(2, viewModel.state.value.mealsDay)
    }

    @Test
    fun `a generated plan is stored when nothing was saved before`() = runTest {
        val repository = FakeMealPlanRepository()
        val viewModel = viewModel(FakeRecipeRepository(defaultCatalog()), repository)

        viewModel.onAction(MealPlanAction.OnGenerate(MealPlanPreferences(mealsDay = 3, people = 2)))

        assertTrue(viewModel.state.value.hasPlan)
        assertEquals(1, repository.savedCount)
    }

    private fun savedPlan(): MealPlan {
        val today = LocalDate.now()
        val catalog = defaultCatalog()
        return MealPlan(
            startDate = today,
            days = (0 until 7).map { offset ->
                PlanDay(
                    date = today.plusDays(offset.toLong()),
                    meals = listOf(catalog[offset % catalog.size]),
                )
            },
            preferences = MealPlanPreferences(mealsDay = 1, people = 3),
        )
    }

    private fun viewModel(
        repository: RecipeRepository,
        mealPlanRepository: MealPlanRepository = FakeMealPlanRepository(),
    ) = MealPlanViewModel(
        GenerateMealPlanUseCase(repository),
        FakeBillingRepository(),
        mealPlanRepository,
    )

    private class FakeMealPlanRepository(
        private var plan: MealPlan? = null,
    ) : MealPlanRepository {
        var savedCount = 0
            private set

        override suspend fun getSavedPlan(): MealPlan? = plan
        override suspend fun savePlan(plan: MealPlan) {
            this.plan = plan
            savedCount++
        }

        override suspend fun clearPlan() {
            plan = null
        }
    }

    private class FakeBillingRepository : BillingRepository {
        override val isChef = MutableStateFlow(true)
        override fun startObserving() = Unit
        override suspend fun getChefOffers(): List<ChefOffer> = emptyList()
        override suspend fun purchaseChef(
            activityContext: Context,
            plan: ChefPlan,
        ): PurchaseOutcome = PurchaseOutcome.Cancelled

        override suspend fun restorePurchases(): Result = Result.Success
        override suspend fun logIn(userId: String) = Unit
        override fun setUserAttributes(email: String?, displayName: String?) = Unit
        override suspend fun logOut() = Unit
    }

    private fun defaultCatalog(): List<Recipe> = listOf(
        Recipe(
            id = "r1",
            title = "Spicy Chicken Stir-Fry",
            ingredients = listOf(Ingredient("Chicken"), Ingredient("Rice")),
            allergies = emptyList(),
            preparationMinutes = 10,
            cookMinutes = 30,
            chips = emptyList(),
            difficulty = "Medium",
            instructions = emptyList(),
            titleImg = "",
        ),
        Recipe(
            id = "r2",
            title = "Vegetable Curry",
            ingredients = listOf(Ingredient("Spinach"), Ingredient("Rice")),
            allergies = emptyList(),
            preparationMinutes = 15,
            cookMinutes = 25,
            chips = emptyList(),
            difficulty = "Easy",
            instructions = emptyList(),
            titleImg = "",
        ),
    )

    private class FakeRecipeRepository(
        private val recipes: List<Recipe>,
        private var failFirst: Boolean = false,
    ) : RecipeRepository {
        override suspend fun getRecipes(): List<Recipe> {
            if (failFirst) {
                failFirst = false
                throw RuntimeException("Firestore unavailable")
            }
            return recipes
        }
    }
}
