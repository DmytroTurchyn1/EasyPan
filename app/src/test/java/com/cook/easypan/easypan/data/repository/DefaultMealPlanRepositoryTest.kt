package com.cook.easypan.easypan.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.cook.easypan.app.dataStore
import com.cook.easypan.easypan.data.datastore.AppSettings
import com.cook.easypan.easypan.data.datastore.AppSettingsSerializer
import com.cook.easypan.easypan.domain.model.MealPlan
import com.cook.easypan.easypan.domain.model.MealPlanPreferences
import com.cook.easypan.easypan.domain.model.PlanDay
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.domain.repository.RecipeRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DefaultMealPlanRepositoryTest {

    @RelaxedMockK
    private lateinit var context: Context

    @RelaxedMockK
    private lateinit var recipeRepository: RecipeRepository

    private lateinit var repository: DefaultMealPlanRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        repository = DefaultMealPlanRepository(
            context = context,
            recipeRepository = recipeRepository
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun recipe(id: String) = Recipe(
        id = id,
        title = "Recipe $id",
        ingredients = emptyList(),
        preparationMinutes = 5,
        cookMinutes = 10,
        difficulty = "Easy",
        instructions = emptyList(),
        titleImg = ""
    )

    private fun plan(startDate: LocalDate, recipeIds: List<String>) = MealPlan(
        startDate = startDate,
        days = recipeIds.mapIndexed { index, id ->
            PlanDay(date = startDate.plusDays(index.toLong()), meals = listOf(recipe(id)))
        },
        preferences = MealPlanPreferences(mealsDay = 1, people = 2)
    )

    private fun testDataStore(): Pair<DataStore<AppSettings>, CoroutineScope> {
        mockkStatic("com.cook.easypan.app.EasyPanAppKt")
        val tmpFile = File.createTempFile("settings_test", ".json")
        tmpFile.delete()
        val scope = CoroutineScope(Dispatchers.IO + Job())
        val store: DataStore<AppSettings> = DataStoreFactory.create(
            serializer = AppSettingsSerializer,
            scope = scope
        ) { tmpFile }
        every { context.dataStore } returns store
        return store to scope
    }

    @Test
    fun `savePlan then getSavedPlan returns an equal plan`() = runBlocking {
        val (_, scope) = testDataStore()
        val today = LocalDate.now()
        val saved = plan(today, listOf("a", "b"))
        coEvery { recipeRepository.getRecipes() } returns listOf(recipe("a"), recipe("b"))

        repository.savePlan(saved)
        val result = repository.getSavedPlan()

        assertEquals(saved, result)
        scope.cancel()
    }

    @Test
    fun `savePlan stores recipe ids rather than whole recipes`() = runBlocking {
        val (store, scope) = testDataStore()
        val today = LocalDate.now()

        repository.savePlan(plan(today, listOf("a")))

        val dto = store.data.first().savedMealPlan
        assertEquals(listOf("a"), dto?.days?.first()?.recipeIds)
        scope.cancel()
    }

    @Test
    fun `getSavedPlan returns null when a recipe is missing from the catalog`() = runBlocking {
        val (store, scope) = testDataStore()
        val today = LocalDate.now()
        repository.savePlan(plan(today, listOf("a", "gone")))
        coEvery { recipeRepository.getRecipes() } returns listOf(recipe("a"))

        val result = repository.getSavedPlan()

        assertNull(result)
        assertNull(store.data.first().savedMealPlan)
        scope.cancel()
    }

    @Test
    fun `getSavedPlan returns null when the plan week has elapsed`() = runBlocking {
        val (store, scope) = testDataStore()
        val start = LocalDate.now().minusDays(7)
        repository.savePlan(plan(start, listOf("a")))
        coEvery { recipeRepository.getRecipes() } returns listOf(recipe("a"))

        val result = repository.getSavedPlan()

        assertNull(result)
        assertNull(store.data.first().savedMealPlan)
        scope.cancel()
    }

    @Test
    fun `getSavedPlan still returns a plan on its final day`() = runBlocking {
        val (_, scope) = testDataStore()
        val start = LocalDate.now().minusDays(6)
        repository.savePlan(plan(start, listOf("a")))
        coEvery { recipeRepository.getRecipes() } returns listOf(recipe("a"))

        assertEquals(start, repository.getSavedPlan()?.startDate)
        scope.cancel()
    }

    @Test
    fun `getSavedPlan returns null when nothing was saved`() = runBlocking {
        val (_, scope) = testDataStore()

        assertNull(repository.getSavedPlan())
        scope.cancel()
    }

    @Test
    fun `clearPlan removes the stored plan`() = runBlocking {
        val (store, scope) = testDataStore()
        repository.savePlan(plan(LocalDate.now(), listOf("a")))

        repository.clearPlan()

        assertNull(store.data.first().savedMealPlan)
        scope.cancel()
    }
}
