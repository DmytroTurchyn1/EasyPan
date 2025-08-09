@file:Suppress("UnusedFlow")

package com.cook.easypan.easypan.data.repository

import android.content.Context
import android.util.Log
import com.cook.easypan.core.domain.AuthResponse
import com.cook.easypan.easypan.data.auth.AuthClient
import com.cook.easypan.easypan.data.database.FirestoreClient
import com.cook.easypan.easypan.data.dto.UserDto
import com.cook.easypan.easypan.domain.model.User
import com.cook.easypan.easypan.domain.model.UserData
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
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

    private lateinit var defaultUserRepository: DefaultUserRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0

        defaultUserRepository = DefaultUserRepository(
            firestoreDataSource = firestoreDataSource,
            googleAuthClient = googleAuthClient
        )
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
        val testUserData = UserData(recipesCooked = 10)
        coEvery { firestoreDataSource.updateUserData("testUserId", any()) } returns Unit

        val result = defaultUserRepository.updateUserData("testUserId", testUserData)

        assertEquals(AuthResponse.Success, result)
    }

    @Test
    fun `updateUserData handles Firestore error during update`() = runBlocking {
        val testUserData = UserData(recipesCooked = 10)
        coEvery {
            firestoreDataSource.updateUserData(
                "testUserId",
                any()
            )
        } throws Exception("Permission denied")

        val result = defaultUserRepository.updateUserData("testUserId", testUserData)

        assertEquals(AuthResponse.Failure("Permission denied"), result)
    }

    @Test
    fun `updateUserData handles unknown error during update`() = runBlocking {
        val testUserData = UserData(recipesCooked = 10)
        coEvery {
            firestoreDataSource.updateUserData(
                "testUserId",
                any()
            )
        } throws Exception("Unknown error")

        val result = defaultUserRepository.updateUserData("testUserId", testUserData)

        assertEquals(AuthResponse.Failure("Unknown error"), result)
    }

    @Test
    fun `getCurrentUser when user is signed in and data fetch succeeds`() = runBlocking {
        val baseUser = User(userId = "testUserId", username = "testUser")
        val userData = UserData(recipesCooked = 5)
        val userDto = UserDto(recipesCooked = 5)

        every { googleAuthClient.getSignedInUser() } returns baseUser
        coEvery { firestoreDataSource.getUserData("testUserId") } returns userDto

        val result = defaultUserRepository.getCurrentUser()

        assertEquals(baseUser.copy(data = userData), result)
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

        val result = defaultUserRepository.getCurrentUser()

        assertEquals(baseUser, result)
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
    fun `signOut calls googleAuthClient signOut`() {
        every { googleAuthClient.signOut() } returns Unit

        defaultUserRepository.signOut()

        verify { googleAuthClient.signOut() }
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
    fun `signInWithGoogle successfully initiates sign in flow`() = runBlocking {
        val mockContext = mockk<Context>()
        val authResponseFlow = flowOf(AuthResponse.Success)

        every { googleAuthClient.signInWithGoogle(mockContext) } returns authResponseFlow

        val result = defaultUserRepository.signInWithGoogle(mockContext)

        assertEquals(AuthResponse.Success, result.first())
    }

    @Test
    fun `signInWithGoogle flow emits AuthResponse Success`() = runBlocking {
        val mockContext = mockk<Context>()
        val authResponseFlow = flowOf(AuthResponse.Success)

        every { googleAuthClient.signInWithGoogle(mockContext) } returns authResponseFlow

        val result = defaultUserRepository.signInWithGoogle(mockContext)

        assertEquals(AuthResponse.Success, result.first())
    }

    @Test
    fun `signInWithGoogle flow emits AuthResponse Failure`() = runBlocking {
        val mockContext = mockk<Context>()
        val authResponseFlow = flowOf(AuthResponse.Failure("Sign in failed"))

        every { googleAuthClient.signInWithGoogle(mockContext) } returns authResponseFlow

        val result = defaultUserRepository.signInWithGoogle(mockContext)

        assertEquals(AuthResponse.Failure("Sign in failed"), result.first())
    }

    @Test
    fun `signInWithGoogle handles exceptions from googleAuthClient`(): Unit = runBlocking {
        val mockContext = mockk<Context>()

        every { googleAuthClient.signInWithGoogle(mockContext) } throws Exception("Auth configuration error")

        assertFailsWith<Exception> {
            defaultUserRepository.signInWithGoogle(mockContext)
        }
    }

    @Test
    fun `signInWithGoogle with invalid context throws exception`(): Unit = runBlocking {
        val mockContext = mockk<Context>()

        every { googleAuthClient.signInWithGoogle(mockContext) } throws IllegalArgumentException("Invalid context configuration")

        assertFailsWith<IllegalArgumentException> {
            defaultUserRepository.signInWithGoogle(mockContext)
        }
    }

}