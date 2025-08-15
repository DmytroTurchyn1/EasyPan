/*
 * Created  15/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.data.auth

import android.content.Context
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthClientTest {

    @RelaxedMockK
    private lateinit var applicationContext: Context

    @RelaxedMockK
    private lateinit var firebaseAuth: FirebaseAuth

    @RelaxedMockK
    private lateinit var mockFirebaseUser: FirebaseUser


    @RelaxedMockK
    private lateinit var mockAuthCredential: AuthCredential

    @RelaxedMockK
    private lateinit var mockTask: Task<AuthResult>

    private lateinit var authClient: AuthClient

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        authClient = AuthClient(applicationContext, firebaseAuth)
    }

    @Test
    fun `test nonce creation`() {
        val nonce = authClient.createNonce()
        assertNotNull(nonce)
        assertEquals(64, nonce.length)
        assertTrue(nonce.matches(Regex("[a-f0-9]{64}")))
    }

    @Test
    fun `test call getSignedInUser function`() {
        every { firebaseAuth.currentUser } returns null

        val user = authClient.getSignedInUser()

        assertNull(user)
        verify(exactly = 1) { firebaseAuth.currentUser }
    }

    @Test
    fun `test return getSignedInUser function`() {

        every { mockFirebaseUser.uid } returns "testUserId"
        every { mockFirebaseUser.displayName } returns "testUsername"
        every { mockFirebaseUser.photoUrl } returns null
        every { firebaseAuth.currentUser } returns mockFirebaseUser

        val user = authClient.getSignedInUser()

        assertEquals("testUserId", user?.userId)
        assertEquals("testUsername", user?.username)
        assertNull(user?.profilePictureUrl)
    }

    @Test
    fun `test Firebase Auth successful sign in behavior`() = runBlocking {
        mockkStatic(GoogleAuthProvider::class)
        every { GoogleAuthProvider.getCredential("mock_id_token", null) } returns mockAuthCredential
        every { firebaseAuth.signInWithCredential(mockAuthCredential) } returns mockTask
        every { mockTask.isSuccessful } returns true
        val listenerSlot = slot<OnCompleteListener<AuthResult>>()
        every { mockTask.addOnCompleteListener(capture(listenerSlot)) } answers {
            listenerSlot.captured.onComplete(mockTask)
            mockTask
        }

        val credential = GoogleAuthProvider.getCredential("mock_id_token", null)
        val task = firebaseAuth.signInWithCredential(credential)

        task.addOnCompleteListener { completedTask ->
            assertTrue(completedTask.isSuccessful)
        }

        verify { firebaseAuth.signInWithCredential(mockAuthCredential) }
        verify { mockTask.addOnCompleteListener(any()) }
    }
}