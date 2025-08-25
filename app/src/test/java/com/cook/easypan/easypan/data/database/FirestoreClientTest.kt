/*
 * Created  25/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.data.database

import android.util.Log
import com.cook.easypan.core.util.USER_DATA_COLLECTION
import com.cook.easypan.easypan.data.dto.RecipeDto
import com.cook.easypan.easypan.data.dto.UserDto
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import kotlin.test.Test

class FirestoreClientTest {

    private lateinit var firestoreClient: FirestoreClient

    @MockK
    private lateinit var firebaseFirestore: FirebaseFirestore

    @RelaxedMockK
    private lateinit var mockDocument: DocumentSnapshot

    @RelaxedMockK
    private lateinit var mockDocumentRef: DocumentReference

    @RelaxedMockK
    private lateinit var mockCollectionRef: CollectionReference

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        firestoreClient = FirestoreClient(firebaseFirestore)
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getRecipes should return mapped RecipeDto list`() = runBlocking {

        val expectedRecipe =
            RecipeDto(title = "Test Recipe", ingredients = listOf("Eggs", "Milk"), id = "")

        every { mockDocument.id } returns "abc123"
        every { mockDocument.toObject(RecipeDto::class.java) } returns expectedRecipe

        val firestoreClientSpy = spyk(firestoreClient, recordPrivateCalls = true)

        coEvery { firestoreClientSpy["getCollection"]("Recipes") } returns listOf(mockDocument)

        val result = firestoreClientSpy.getRecipes()

        assertEquals(1, result.size)
        assertEquals("abc123", result[0].id)
        assertEquals("Test Recipe", result[0].title)
        assertEquals(listOf("Eggs", "Milk"), result[0].ingredients)
    }

    @Test
    fun `getUserData should return mapped UserDto when user exists`() = runBlocking {
        val userId = "testUserId"
        val expectedUser = UserDto(recipesCooked = 3)
        val completedTask = mockk<Task<DocumentSnapshot>>(relaxed = true) {
            every { isComplete } returns true
            every { isSuccessful } returns true
            every { result } returns mockDocument
            every { isCanceled } returns false
            every { exception } returns null
        }

        every { firebaseFirestore.collection(USER_DATA_COLLECTION) } returns mockCollectionRef
        every { mockCollectionRef.document(userId) } returns mockDocumentRef
        every { mockDocumentRef.get() } returns completedTask
        every { mockDocument.exists() } returns true
        every { mockDocument.toObject(UserDto::class.java) } returns expectedUser

        val result = firestoreClient.getUserData(userId)

        assertEquals(3, result.recipesCooked)
    }

    @Test
    fun `getUserData should create new user when user does not exist`() = runBlocking {
        val userId = "missingUser"

        val completedGetTask = mockk<Task<DocumentSnapshot>>(relaxed = true) {
            every { isComplete } returns true
            every { isSuccessful } returns true
            every { result } returns mockDocument
            every { isCanceled } returns false
            every { exception } returns null
        }
        val completedSetTask = mockk<Task<Void>>(relaxed = true) {
            every { isComplete } returns true
            every { isSuccessful } returns true
            every { result } returns null
            every { isCanceled } returns false
            every { exception } returns null
        }

        val capturedUser = slot<UserDto>()
        val capturedOptions = slot<SetOptions>()

        every { firebaseFirestore.collection(USER_DATA_COLLECTION) } returns mockCollectionRef
        every { mockCollectionRef.document(userId) } returns mockDocumentRef
        every { mockDocumentRef.get() } returns completedGetTask
        every { mockDocument.exists() } returns false
        every {
            mockDocumentRef.set(
                capture(capturedUser),
                capture(capturedOptions)
            )
        } returns completedSetTask

        val result = firestoreClient.getUserData(userId)

        assertEquals(0, result.recipesCooked)
        assertEquals(UserDto(0), capturedUser.captured)
        verify(exactly = 1) { mockDocumentRef.get() }
        verify(exactly = 1) { mockDocumentRef.set(any<UserDto>(), any<SetOptions>()) }
    }

    @Test
    fun `writes to Users collection with correct Id and Data`() = runBlocking {
        val userId = "user123"
        val userData = UserDto(recipesCooked = 7)

        val completedSetTask = mockk<Task<Void>>(relaxed = true) {
            every { isComplete } returns true
            every { isSuccessful } returns true
            every { result } returns null
            every { isCanceled } returns false
            every { exception } returns null
        }

        val capturedUser = slot<UserDto>()
        val capturedOptions = slot<SetOptions>()

        every { firebaseFirestore.collection(USER_DATA_COLLECTION) } returns mockCollectionRef
        every { mockCollectionRef.document(userId) } returns mockDocumentRef
        every {
            mockDocumentRef.set(
                capture(capturedUser),
                capture(capturedOptions)
            )
        } returns completedSetTask

        firestoreClient.incrementCookedRecipes(userId)

        assertEquals(userData, capturedUser.captured)
        verify(exactly = 1) { firebaseFirestore.collection(USER_DATA_COLLECTION) }
        verify(exactly = 1) { mockCollectionRef.document(userId) }
        verify(exactly = 1) { mockDocumentRef.set(any<UserDto>(), any<SetOptions>()) }
    }

}