/*
 * Created  25/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.cook.easypan.app.dataStore
import com.cook.easypan.easypan.data.database.FirestoreClient
import com.cook.easypan.easypan.data.datastore.AppSettings
import com.cook.easypan.easypan.data.datastore.AppSettingsSerializer
import com.cook.easypan.easypan.data.dto.RecipeDto
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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DefaultRecipeRepositoryTest {

    @RelaxedMockK
    private lateinit var firestoreDataSource: FirestoreClient

    @RelaxedMockK
    private lateinit var context: Context

    private lateinit var defaultRecipeRepository: DefaultRecipeRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        defaultRecipeRepository = DefaultRecipeRepository(
            firestoreDataSource = firestoreDataSource,
            context = context
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getRecipes returns cached recipes when cache is valid`() = runBlocking {
        val recipeDto = RecipeDto(id = "recipe1", title = "Test Recipe", difficulty = 1)

        mockkStatic("com.cook.easypan.app.EasyPanAppKt")
        val tmpFile = File.createTempFile("settings_test", ".json")
        val scope = CoroutineScope(Dispatchers.IO + Job())
        val testDataStore: DataStore<AppSettings> = DataStoreFactory.create(
            serializer = AppSettingsSerializer,
            scope = scope
        ) { tmpFile }

        // Set cache with recent timestamp
        testDataStore.updateData { settings ->
            settings.copy(
                lastCacheTimeRecipes = System.currentTimeMillis(),
                cachedRecipes = listOf(recipeDto)
            )
        }
        every { context.dataStore } returns testDataStore

        val result = defaultRecipeRepository.getRecipes()

        assertEquals(1, result.size)
        assertEquals("recipe1", result[0].id)
        assertEquals("Test Recipe", result[0].title)
        scope.cancel()
    }

    @Test
    fun `getRecipes fetches from Firestore when cache is stale`() = runBlocking {
        val recipeDto = RecipeDto(id = "recipe1", title = "Test Recipe", difficulty = 1)
        coEvery { firestoreDataSource.getRecipes() } returns listOf(recipeDto)

        mockkStatic("com.cook.easypan.app.EasyPanAppKt")
        val tmpFile = File.createTempFile("settings_test", ".json")
        val scope = CoroutineScope(Dispatchers.IO + Job())
        val testDataStore: DataStore<AppSettings> = DataStoreFactory.create(
            serializer = AppSettingsSerializer,
            scope = scope
        ) { tmpFile }
        every { context.dataStore } returns testDataStore

        val result = defaultRecipeRepository.getRecipes()

        assertEquals(1, result.size)
        assertEquals("recipe1", result[0].id)
        assertEquals("Test Recipe", result[0].title)
        scope.cancel()
    }

    @Test
    fun `getRecipes fetches from Firestore when cache is empty`() = runBlocking {
        val recipeDto = RecipeDto(id = "recipe1", title = "Test Recipe", difficulty = 1)
        coEvery { firestoreDataSource.getRecipes() } returns listOf(recipeDto)

        mockkStatic("com.cook.easypan.app.EasyPanAppKt")
        val tmpFile = File.createTempFile("settings_test", ".json")
        val scope = CoroutineScope(Dispatchers.IO + Job())
        val testDataStore: DataStore<AppSettings> = DataStoreFactory.create(
            serializer = AppSettingsSerializer,
            scope = scope
        ) { tmpFile }

        // Cache is empty (default state)
        every { context.dataStore } returns testDataStore

        val result = defaultRecipeRepository.getRecipes()

        assertEquals(1, result.size)
        assertEquals("recipe1", result[0].id)
        assertEquals("Test Recipe", result[0].title)
        scope.cancel()
    }

    @Test
    fun `getRecipes success with empty list from Firestore`() = runBlocking {
        coEvery { firestoreDataSource.getRecipes() } returns emptyList()

        mockkStatic("com.cook.easypan.app.EasyPanAppKt")
        val tmpFile = File.createTempFile("settings_test", ".json")
        val scope = CoroutineScope(Dispatchers.IO + Job())
        val testDataStore: DataStore<AppSettings> = DataStoreFactory.create(
            serializer = AppSettingsSerializer,
            scope = scope
        ) { tmpFile }
        every { context.dataStore } returns testDataStore

        val result = defaultRecipeRepository.getRecipes()

        assertTrue(result.isEmpty())
        scope.cancel()
    }

    @Test
    fun `getRecipes success with multiple recipes from Firestore`() = runBlocking {
        val recipes = listOf(
            RecipeDto(id = "recipe1", title = "Recipe 1", difficulty = 1),
            RecipeDto(id = "recipe2", title = "Recipe 2", difficulty = 2),
            RecipeDto(id = "recipe3", title = "Recipe 3", difficulty = 3)
        )
        coEvery { firestoreDataSource.getRecipes() } returns recipes

        mockkStatic("com.cook.easypan.app.EasyPanAppKt")
        val tmpFile = File.createTempFile("settings_test", ".json")
        val scope = CoroutineScope(Dispatchers.IO + Job())
        val testDataStore: DataStore<AppSettings> = DataStoreFactory.create(
            serializer = AppSettingsSerializer,
            scope = scope
        ) { tmpFile }
        every { context.dataStore } returns testDataStore

        val result = defaultRecipeRepository.getRecipes()

        assertEquals(3, result.size)
        assertEquals("recipe1", result[0].id)
        assertEquals("recipe2", result[1].id)
        assertEquals("recipe3", result[2].id)
        scope.cancel()
    }

    @Test
    fun `getRecipes handles Firestore exception`(): Unit = runBlocking {
        coEvery { firestoreDataSource.getRecipes() } throws Exception("Firestore error")

        mockkStatic("com.cook.easypan.app.EasyPanAppKt")
        val tmpFile = File.createTempFile("settings_test", ".json")
        val scope = CoroutineScope(Dispatchers.IO + Job())
        val testDataStore: DataStore<AppSettings> = DataStoreFactory.create(
            serializer = AppSettingsSerializer,
            scope = scope
        ) { tmpFile }
        every { context.dataStore } returns testDataStore

        assertFailsWith<Exception> {
            defaultRecipeRepository.getRecipes()
        }
        scope.cancel()
    }

    @Test
    fun `getRecipes updates cache after Firestore fetch`() = runBlocking {
        val recipeDto = RecipeDto(id = "recipe1", title = "Test Recipe", difficulty = 1)
        coEvery { firestoreDataSource.getRecipes() } returns listOf(recipeDto)

        mockkStatic("com.cook.easypan.app.EasyPanAppKt")
        val tmpFile = File.createTempFile("settings_test", ".json")
        val scope = CoroutineScope(Dispatchers.IO + Job())
        val testDataStore: DataStore<AppSettings> = DataStoreFactory.create(
            serializer = AppSettingsSerializer,
            scope = scope
        ) { tmpFile }
        every { context.dataStore } returns testDataStore

        val result = defaultRecipeRepository.getRecipes()

        // Verify the result
        assertEquals(1, result.size)
        assertEquals("recipe1", result[0].id)

        // Verify cache was updated
        val cachedData = testDataStore.data.first()
        assertEquals(1, cachedData.cachedRecipes.size)
        assertEquals("recipe1", cachedData.cachedRecipes[0].id)
        assertTrue(cachedData.lastCacheTimeRecipes > 0)

        scope.cancel()
    }

    @Test
    fun `getRecipes uses cache when available and valid time`() = runBlocking {
        val cachedRecipe = RecipeDto(id = "cached1", title = "Cached Recipe", difficulty = 1)
        val firestoreRecipe =
            RecipeDto(id = "firestore1", title = "Firestore Recipe", difficulty = 2)

        // Setup Firestore to return different data (should not be called)
        coEvery { firestoreDataSource.getRecipes() } returns listOf(firestoreRecipe)

        mockkStatic("com.cook.easypan.app.EasyPanAppKt")
        val tmpFile = File.createTempFile("settings_test", ".json")
        val scope = CoroutineScope(Dispatchers.IO + Job())
        val testDataStore: DataStore<AppSettings> = DataStoreFactory.create(
            serializer = AppSettingsSerializer,
            scope = scope
        ) { tmpFile }

        // Set cache with very recent timestamp (should be valid)
        testDataStore.updateData { settings ->
            settings.copy(
                lastCacheTimeRecipes = System.currentTimeMillis() - 1000, // 1 second ago
                cachedRecipes = listOf(cachedRecipe)
            )
        }
        every { context.dataStore } returns testDataStore

        val result = defaultRecipeRepository.getRecipes()

        // Should return cached data, not Firestore data
        assertEquals(1, result.size)
        assertEquals("cached1", result[0].id)
        assertEquals("Cached Recipe", result[0].title)

        scope.cancel()
    }
}