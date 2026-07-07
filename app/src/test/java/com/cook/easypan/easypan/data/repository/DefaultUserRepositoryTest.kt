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
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.core.domain.Result
import com.cook.easypan.easypan.data.auth.AuthClient
import com.cook.easypan.easypan.data.database.FirestoreClient
import com.cook.easypan.easypan.data.datastore.AppSettings
import com.cook.easypan.easypan.data.datastore.AppSettingsSerializer
import com.cook.easypan.easypan.data.dto.RecipeDto
import com.cook.easypan.easypan.data.dto.UserDto
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.domain.model.User
import com.cook.easypan.easypan.domain.model.UserData
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultUserRepositoryTest {

    @RelaxedMockK
    private lateinit var firestoreDataSource: FirestoreClient

    @RelaxedMockK
    private lateinit var googleAuthClient: AuthClient

    @RelaxedMockK
    private lateinit var context: Context

    private lateinit var defaultUserRepository: DefaultUserRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0

        defaultUserRepository = DefaultUserRepository(
            firestoreDataSource = firestoreDataSource,
            googleAuthClient = googleAuthClient,
            context = context
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun createTestDataStore(scope: CoroutineScope): DataStore<AppSettings> {
        mockkStatic("com.cook.easypan.app.EasyPanAppKt")
        val tmpFile = File.createTempFile("settings_test", ".json")
        val testDataStore = DataStoreFactory.create(
            serializer = AppSettingsSerializer,
            scope = scope
        ) { tmpFile }
        every { context.dataStore } returns testDataStore
        return testDataStore
    }

    @Test
    fun `getUserData successfully fetches user data`() = runBlocking {
        coEvery { firestoreDataSource.getUserData("testUserId") } returns UserDto(recipesCooked = 5)

        val user = defaultUserRepository.getUserData("testUserId")
        assertEquals(5, user.recipesCooked)
    }

    @Test
    fun `getUserData handles non existent user`(): Unit = runBlocking {
        coEvery { firestoreDataSource.getUserData("invalidId") } throws Exception("Document does not exist")

        assertFailsWith<Exception> {
            defaultUserRepository.getUserData("invalidId")
        }
    }

    @Test
    fun `getUserData handles Firestore error`(): Unit = runBlocking {
        coEvery { firestoreDataSource.getUserData("testUserId") } throws Exception("Network error")

        assertFailsWith<Exception> {
            defaultUserRepository.getUserData("testUserId")
        }
    }

    @Test
    fun `getUserData handles empty or invalid userId`(): Unit = runBlocking {
        coEvery { firestoreDataSource.getUserData("") } throws Exception("Invalid user ID")

        assertFailsWith<Exception> {
            defaultUserRepository.getUserData("")
        }
    }

    @Test
    fun `updateUserData successfully updates user data`() = runBlocking {
        val user = User(userId = "testUserId", username = "testUser")
        every { googleAuthClient.getSignedInUser() } returns user
        coEvery { firestoreDataSource.incrementCookedRecipes("testUserId") } returns Unit

        val scope = CoroutineScope(Dispatchers.IO + Job())
        createTestDataStore(scope)

        val result = defaultUserRepository.updateUserData()

        assertEquals(Result.Success, result)
        coVerify { firestoreDataSource.incrementCookedRecipes("testUserId") }
        scope.cancel()
    }

    @Test
    fun `updateUserData returns Failure when user not logged in`() = runBlocking {
        every { googleAuthClient.getSignedInUser() } returns null

        val result = defaultUserRepository.updateUserData()

        assertEquals(Result.Failure(AppError.NOT_SIGNED_IN), result)
    }

    @Test
    fun `updateUserData handles Firestore error during update`() = runBlocking {
        val user = User(userId = "testUserId", username = "testUser")
        every { googleAuthClient.getSignedInUser() } returns user
        coEvery { firestoreDataSource.incrementCookedRecipes("testUserId") } throws Exception("Permission denied")

        val result = defaultUserRepository.updateUserData()

        assertEquals(Result.Failure(AppError.UNKNOWN), result)
    }

    @Test
    fun `getCurrentUser when user is signed in and data fetch succeeds`() = runBlocking {
        val baseUser = User(userId = "testUserId", username = "testUser")
        val userData = UserData(recipesCooked = 5)
        val userDto = UserDto(recipesCooked = 5)

        every { googleAuthClient.getSignedInUser() } returns baseUser
        coEvery { firestoreDataSource.getUserData("testUserId") } returns userDto

        val scope = CoroutineScope(Dispatchers.IO + Job())
        createTestDataStore(scope)

        val result = defaultUserRepository.getCurrentUser()

        assertEquals(baseUser.copy(data = userData), result)
        scope.cancel()
    }

    @Test
    fun `getCurrentUser when user is not signed in`() = runBlocking {
        every { googleAuthClient.getSignedInUser() } returns null

        val result = defaultUserRepository.getCurrentUser()

        assertNull(result)
    }

    @Test
    fun `getCurrentUser when user is signed in but data fetch fails`() = runBlocking {
        val baseUser = User(userId = "testUserId", username = "testUser")

        every { googleAuthClient.getSignedInUser() } returns baseUser
        coEvery { firestoreDataSource.getUserData("testUserId") } throws Exception("Failed to fetch data")

        val scope = CoroutineScope(Dispatchers.IO + Job())
        createTestDataStore(scope)

        val result = defaultUserRepository.getCurrentUser()

        assertEquals(baseUser, result)
        scope.cancel()
    }

    @Test
    fun `getCurrentUser handles exceptions from googleAuthClient getSignedInUser`(): Unit =
        runBlocking {
            every { googleAuthClient.getSignedInUser() } throws Exception("Auth client error")

            assertFailsWith<Exception> {
                defaultUserRepository.getCurrentUser()
            }
        }

    @Test
    fun `signOut signs out and clears the local cache`() = runBlocking {
        coEvery { googleAuthClient.signOut() } returns Unit

        val scope = CoroutineScope(Dispatchers.IO + Job())
        val testDataStore = createTestDataStore(scope)
        testDataStore.updateData {
            it.copy(
                userId = "user1",
                userName = "tester",
                cacheFavoriteRecipes = listOf(RecipeDto(id = "recipe1")),
                lastCacheTimeFavorites = System.currentTimeMillis()
            )
        }

        defaultUserRepository.signOut()

        coVerify { googleAuthClient.signOut() }
        assertEquals(AppSettings(), testDataStore.data.first())
        scope.cancel()
    }

    @Test
    fun `isUserSignedIn when user is signed in`() {
        val user = User(userId = "testUserId", username = "testUser")
        every { googleAuthClient.getSignedInUser() } returns user

        val result = defaultUserRepository.isUserSignedIn()

        assertTrue(result)
    }

    @Test
    fun `isUserSignedIn when user is not signed in`() {
        every { googleAuthClient.getSignedInUser() } returns null

        val result = defaultUserRepository.isUserSignedIn()

        assertFalse(result)
    }

    @Test
    fun `signInWithGoogle returns Success from auth client`() = runBlocking {
        coEvery { googleAuthClient.signInWithGoogle(context) } returns Result.Success

        val result = defaultUserRepository.signInWithGoogle(context)

        assertEquals(Result.Success, result)
    }

    @Test
    fun `signInWithGoogle returns Failure from auth client`() = runBlocking {
        coEvery { googleAuthClient.signInWithGoogle(context) } returns Result.Failure(AppError.AUTH_FAILED)

        val result = defaultUserRepository.signInWithGoogle(context)

        assertEquals(Result.Failure(AppError.AUTH_FAILED), result)
    }

    @Test
    fun `signInWithGoogle handles exceptions from googleAuthClient`(): Unit = runBlocking {
        coEvery { googleAuthClient.signInWithGoogle(context) } throws Exception("Auth configuration error")

        assertFailsWith<Exception> {
            defaultUserRepository.signInWithGoogle(context)
        }
    }

    @Test
    fun `getFavoriteRecipes throws when user not logged in`() {
        runBlocking {
            every { googleAuthClient.getSignedInUser() } returns null
            assertFailsWith<IllegalStateException> { defaultUserRepository.getFavoriteRecipes() }
        }
    }

    @Test
    fun `updateKeepScreenOnDataStore updates value and flow emits updated value`() = runBlocking {
        val scope = CoroutineScope(Dispatchers.IO + Job())
        createTestDataStore(scope)

        defaultUserRepository =
            DefaultUserRepository(firestoreDataSource, googleAuthClient, context)

        val result = defaultUserRepository.updateKeepScreenOnDataStore(false)
        assertFalse(result)
        val flowValue = defaultUserRepository.getKeepScreenOnDataStore().first()
        assertFalse(flowValue)
        scope.cancel()
    }

    @Test
    fun `getKeepScreenOnDataStore emits default true initially`() = runBlocking {
        val scope = CoroutineScope(Dispatchers.IO + Job())
        createTestDataStore(scope)
        defaultUserRepository =
            DefaultUserRepository(firestoreDataSource, googleAuthClient, context)

        val initial = defaultUserRepository.getKeepScreenOnDataStore().first()
        assertTrue(initial)
        scope.cancel()
    }

    @Test
    fun `getFavoriteRecipes returns cached recipes when cache is valid for same user`() =
        runBlocking {
            val user = User(userId = "user1", username = "tester")
            every { googleAuthClient.getSignedInUser() } returns user

            val scope = CoroutineScope(Dispatchers.IO + Job())
            val testDataStore = createTestDataStore(scope)

            // Set cache with recent timestamp, written for the same account
            testDataStore.updateData { settings ->
                settings.copy(
                    userId = "user1",
                    lastCacheTimeFavorites = System.currentTimeMillis(),
                    cacheFavoriteRecipes = listOf(
                        RecipeDto(
                            id = "recipe1",
                            title = "Test Recipe",
                            difficulty = 1
                        )
                    )
                )
            }

            val result = defaultUserRepository.getFavoriteRecipes()

            assertEquals(1, result.size)
            assertEquals("recipe1", result[0].id)
            scope.cancel()
        }

    @Test
    fun `getFavoriteRecipes ignores cache written for another user`() = runBlocking {
        val user = User(userId = "user2", username = "otherTester")
        every { googleAuthClient.getSignedInUser() } returns user

        val freshRecipe = RecipeDto(id = "user2recipe", title = "User2 Recipe", difficulty = 1)
        coEvery { firestoreDataSource.getFavoriteRecipes("user2") } returns listOf(freshRecipe)

        val scope = CoroutineScope(Dispatchers.IO + Job())
        val testDataStore = createTestDataStore(scope)

        // Fresh cache, but it belongs to user1
        testDataStore.updateData { settings ->
            settings.copy(
                userId = "user1",
                lastCacheTimeFavorites = System.currentTimeMillis(),
                cacheFavoriteRecipes = listOf(RecipeDto(id = "user1recipe", difficulty = 1))
            )
        }

        val result = defaultUserRepository.getFavoriteRecipes()

        assertEquals(1, result.size)
        assertEquals("user2recipe", result[0].id)
        coVerify { firestoreDataSource.getFavoriteRecipes("user2") }
        scope.cancel()
    }

    @Test
    fun `getFavoriteRecipes fetches from Firestore when cache is stale`() = runBlocking {
        val user = User(userId = "user1", username = "tester")
        every { googleAuthClient.getSignedInUser() } returns user

        val recipeDto = RecipeDto(id = "recipe1", title = "Test Recipe", difficulty = 1)
        coEvery { firestoreDataSource.getFavoriteRecipes("user1") } returns listOf(recipeDto)

        val scope = CoroutineScope(Dispatchers.IO + Job())
        createTestDataStore(scope)

        val result = defaultUserRepository.getFavoriteRecipes()

        assertEquals(1, result.size)
        assertEquals("recipe1", result[0].id)
        coVerify { firestoreDataSource.getFavoriteRecipes("user1") }
        scope.cancel()
    }

    @Test
    fun `addRecipeToFavorites succeeds and returns Success`() = runBlocking {
        val user = User(userId = "user1", username = "tester")
        every { googleAuthClient.getSignedInUser() } returns user
        coEvery { firestoreDataSource.addRecipeToFavorite("user1", any()) } returns Unit

        val scope = CoroutineScope(Dispatchers.IO + Job())
        createTestDataStore(scope)

        val recipe = Recipe(
            id = "recipe1",
            title = "Test Recipe",
            ingredients = listOf("ingredient1"),
            preparationMinutes = 10,
            cookMinutes = 20,
            difficulty = "Easy",
            instructions = emptyList(),
            titleImg = "test.jpg"
        )
        val result = defaultUserRepository.addRecipeToFavorites(recipe)

        assertEquals(Result.Success, result)
        scope.cancel()
    }

    @Test
    fun `addRecipeToFavorites does not duplicate the recipe in the cache`() = runBlocking {
        val user = User(userId = "user1", username = "tester")
        every { googleAuthClient.getSignedInUser() } returns user
        coEvery { firestoreDataSource.addRecipeToFavorite("user1", any()) } returns Unit

        val scope = CoroutineScope(Dispatchers.IO + Job())
        val testDataStore = createTestDataStore(scope)
        testDataStore.updateData { settings ->
            settings.copy(
                userId = "user1",
                cacheFavoriteRecipes = listOf(RecipeDto(id = "recipe1", difficulty = 1))
            )
        }

        val recipe = Recipe(
            id = "recipe1",
            title = "Test Recipe",
            ingredients = listOf("ingredient1"),
            preparationMinutes = 10,
            cookMinutes = 20,
            difficulty = "Easy",
            instructions = emptyList(),
            titleImg = "test.jpg"
        )
        val result = defaultUserRepository.addRecipeToFavorites(recipe)

        assertEquals(Result.Success, result)
        val cached = testDataStore.data.first().cacheFavoriteRecipes
        assertEquals(1, cached.count { it.id == "recipe1" })
        scope.cancel()
    }

    @Test
    fun `addRecipeToFavorites returns Failure when user not logged in`() = runBlocking {
        every { googleAuthClient.getSignedInUser() } returns null
        val recipe = Recipe(
            id = "recipe1",
            title = "Test Recipe",
            ingredients = listOf("ingredient1"),
            preparationMinutes = 10,
            cookMinutes = 20,
            difficulty = "Easy",
            instructions = emptyList(),
            titleImg = "test.jpg"
        )

        val result = defaultUserRepository.addRecipeToFavorites(recipe)

        assertEquals(Result.Failure(AppError.NOT_SIGNED_IN), result)
    }

    @Test
    fun `deleteRecipeFromFavorites returns Success when Firestore delete succeeds`() =
        runBlocking {
            val user = User(userId = "user1", username = "tester")
            every { googleAuthClient.getSignedInUser() } returns user
            coEvery { firestoreDataSource.deleteRecipeFromFavorite("user1", "r1") } returns Unit

            val scope = CoroutineScope(Dispatchers.IO + Job())
            createTestDataStore(scope)

            val result = defaultUserRepository.deleteRecipeFromFavorites("r1")

            assertEquals(Result.Success, result)
            scope.cancel()
        }

    @Test
    fun `deleteRecipeFromFavorites returns Failure when Firestore delete fails`() = runBlocking {
        val user = User(userId = "user1", username = "tester")
        every { googleAuthClient.getSignedInUser() } returns user
        coEvery {
            firestoreDataSource.deleteRecipeFromFavorite("user1", "r1")
        } throws Exception("Permission denied")

        val scope = CoroutineScope(Dispatchers.IO + Job())
        createTestDataStore(scope)

        val result = defaultUserRepository.deleteRecipeFromFavorites("r1")

        assertEquals(Result.Failure(AppError.UNKNOWN), result)
        scope.cancel()
    }

    @Test
    fun `deleteRecipeFromFavorites returns Failure when user not logged in`() = runBlocking {
        every { googleAuthClient.getSignedInUser() } returns null

        val result = defaultUserRepository.deleteRecipeFromFavorites("r1")

        assertEquals(Result.Failure(AppError.NOT_SIGNED_IN), result)
    }

    @Test
    fun `deleteAccount deletes remote data, account and local cache`() = runBlocking {
        val user = User(userId = "user1", username = "tester")
        val activityContext = mockk<Context>(relaxed = true)
        every { googleAuthClient.getSignedInUser() } returns user
        coEvery { firestoreDataSource.deleteUserData("user1") } returns Unit
        coEvery { googleAuthClient.deleteAccount(activityContext) } returns Result.Success

        val scope = CoroutineScope(Dispatchers.IO + Job())
        val testDataStore = createTestDataStore(scope)
        testDataStore.updateData { it.copy(userId = "user1", userName = "tester") }

        val result = defaultUserRepository.deleteAccount(activityContext)

        assertEquals(Result.Success, result)
        coVerify { firestoreDataSource.deleteUserData("user1") }
        coVerify { googleAuthClient.deleteAccount(activityContext) }
        assertEquals(AppSettings(), testDataStore.data.first())
        scope.cancel()
    }

    @Test
    fun `deleteAccount returns Failure when auth deletion fails`() = runBlocking {
        val user = User(userId = "user1", username = "tester")
        val activityContext = mockk<Context>(relaxed = true)
        every { googleAuthClient.getSignedInUser() } returns user
        coEvery { firestoreDataSource.deleteUserData("user1") } returns Unit
        coEvery { googleAuthClient.deleteAccount(activityContext) } returns
                Result.Failure(AppError.REAUTH_REQUIRED)

        val scope = CoroutineScope(Dispatchers.IO + Job())
        createTestDataStore(scope)

        val result = defaultUserRepository.deleteAccount(activityContext)

        assertEquals(Result.Failure(AppError.REAUTH_REQUIRED), result)
        scope.cancel()
    }

    @Test
    fun `deleteAccount returns Failure when user not logged in`() = runBlocking {
        every { googleAuthClient.getSignedInUser() } returns null

        val result = defaultUserRepository.deleteAccount(mockk(relaxed = true))

        assertEquals(Result.Failure(AppError.NOT_SIGNED_IN), result)
    }

    @Test
    fun `isRecipeFavorite returns true when Firestore reports favorite`() = runBlocking {
        val user = User(userId = "user1", username = "tester")
        every { googleAuthClient.getSignedInUser() } returns user
        coEvery { firestoreDataSource.isRecipeFavorite("user1", "r1") } returns true

        val result = defaultUserRepository.isRecipeFavorite("r1")

        assertTrue(result)
    }

    @Test
    fun `isRecipeFavorite returns false when Firestore reports not favorite`() = runBlocking {
        val user = User(userId = "user1", username = "tester")
        every { googleAuthClient.getSignedInUser() } returns user
        coEvery { firestoreDataSource.isRecipeFavorite("user1", "r1") } returns false

        val result = defaultUserRepository.isRecipeFavorite("r1")

        assertFalse(result)
    }

    @Test
    fun `isRecipeFavorite throws when user not logged in`(): Unit = runBlocking {
        every { googleAuthClient.getSignedInUser() } returns null

        assertFailsWith<IllegalStateException> {
            defaultUserRepository.isRecipeFavorite("r1")
        }
    }
}
