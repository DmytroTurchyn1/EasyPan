/*
 * Created  2/7/2026
 *
 * Copyright (c) 2026 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.authentication

import android.content.Context
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.core.domain.Result
import com.cook.easypan.easypan.domain.repository.UserRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthenticationViewModelTest {

    @RelaxedMockK
    private lateinit var userRepository: UserRepository

    private lateinit var viewModel: AuthenticationViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = AuthenticationViewModel(userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `successful sign in updates state`() = runTest {
        val context = mockk<Context>(relaxed = true)
        coEvery { userRepository.signInWithGoogle(context) } returns Result.Success

        viewModel.onAction(AuthenticationAction.OnAuthButtonClick(context))

        assertTrue(viewModel.state.value.isSignInSuccessful)
        assertNull(viewModel.state.value.signInError)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `success does not wait on a profile fetch before navigating`() = runTest {
        val context = mockk<Context>(relaxed = true)
        coEvery { userRepository.signInWithGoogle(context) } returns Result.Success

        viewModel.onAction(AuthenticationAction.OnAuthButtonClick(context))

        // Loading the profile here would hold the screen on a spinner past the point auth is done,
        // and backing out of that window used to leave a half-signed-in user.
        coVerify(exactly = 0) { userRepository.getCurrentUser() }
        assertTrue(viewModel.state.value.isSignInSuccessful)
    }

    @Test
    fun `failed sign in exposes the error`() = runTest {
        val context = mockk<Context>(relaxed = true)
        coEvery { userRepository.signInWithGoogle(context) } returns
                Result.Failure(AppError.AUTH_FAILED)

        viewModel.onAction(AuthenticationAction.OnAuthButtonClick(context))

        assertFalse(viewModel.state.value.isSignInSuccessful)
        assertEquals(AppError.AUTH_FAILED, viewModel.state.value.signInError)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `cancelled sign in shows no error`() = runTest {
        val context = mockk<Context>(relaxed = true)
        coEvery { userRepository.signInWithGoogle(context) } returns
                Result.Failure(AppError.SIGN_IN_CANCELLED)

        viewModel.onAction(AuthenticationAction.OnAuthButtonClick(context))

        assertFalse(viewModel.state.value.isSignInSuccessful)
        assertNull(viewModel.state.value.signInError)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `resetState restores the initial state`() = runTest {
        val context = mockk<Context>(relaxed = true)
        coEvery { userRepository.signInWithGoogle(context) } returns
                Result.Failure(AppError.AUTH_FAILED)
        viewModel.onAction(AuthenticationAction.OnAuthButtonClick(context))

        viewModel.resetState()

        assertEquals(AuthenticationState(), viewModel.state.value)
    }
}
