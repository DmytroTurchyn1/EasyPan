package com.cook.easypan.easypan.data.database

import com.cook.easypan.easypan.data.dto.RecipeDto
import com.cook.easypan.easypan.data.dto.UserDto
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
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
    }


    @Test
    fun `getRecipes should return mapped RecipeDto list`() = runBlocking {

        val expectedRecipe =
            RecipeDto(title = "Test Recipe", ingredients = listOf("Eggs", "Milk"), id = "")

        every { mockDocument.id } returns "abc123"
        every { mockDocument.toObject(RecipeDto::class.java) } returns expectedRecipe

        val firestoreClientSpy = spyk(firestoreClient)

        coEvery { firestoreClientSpy["getCollection"]("Recipes") } returns listOf(mockDocument)

        val result = firestoreClientSpy.getRecipes()

        assertEquals(1, result.size)
        assertEquals("abc123", result[0].id)
        assertEquals("Test Recipe", result[0].title)
        assertEquals(listOf("Eggs", "Milk"), result[0].ingredients)
    }

    @Test
    fun `getUserData should return mapped UserDto when user exists`() = runBlocking {
        // Given
        val userId = "testUserId"
        val expectedUser = UserDto(recipesCooked = 3)
        val completedTask = mockk<Task<DocumentSnapshot>>(relaxed = true) {
            every { isComplete } returns true
            every { isSuccessful } returns true
            every { result } returns mockDocument
            every { isCanceled } returns false
            every { exception } returns null
        }

        every { firebaseFirestore.collection("Users") } returns mockCollectionRef
        every { mockCollectionRef.document(userId) } returns mockDocumentRef
        every { mockDocumentRef.get() } returns completedTask
        every { mockDocument.exists() } returns true
        every { mockDocument.toObject(UserDto::class.java) } returns expectedUser

        val result = firestoreClient.getUserData(userId)

        assertEquals(3, result.recipesCooked)
    }

    @Test
    fun `getUserData should create new user when user does not exist`() = runBlocking {
        val userId = "testUserId"

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

        every { firebaseFirestore.collection("Users") } returns mockCollectionRef
        every { mockCollectionRef.document(userId) } returns mockDocumentRef
        every { mockDocumentRef.get() } returns completedGetTask
        every { mockDocumentRef.set(any()) } returns completedSetTask
        every { mockDocument.exists() } returns false

        val result = firestoreClient.getUserData(userId)

        assertEquals(0, result.recipesCooked)
    }

    @Test
    fun `writes to Users collection with correct Id and Data`() = runBlocking {
        val defaultUserId = "testUserId"
        val userData = UserDto(recipesCooked = 5)
        val completedTask = Tasks.forResult<Void>(null)
        val userId = slot<String>()

        every { firebaseFirestore.collection("Users") } returns mockCollectionRef
        every { mockCollectionRef.document(capture(userId)) } returns mockDocumentRef
        every { mockDocumentRef.set(userData) } returns completedTask

        firestoreClient.updateUserData(
            userId = defaultUserId,
            userData = userData
        )

        verify(exactly = 1) { mockDocumentRef.set(userData) }
        assertEquals(defaultUserId, userId.captured)

    }

}