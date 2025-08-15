package com.cook.easypan

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cook.easypan.easypan.data.auth.AuthClient
import com.cook.easypan.easypan.data.database.FirestoreClient
import com.cook.easypan.easypan.data.repository.DefaultUserRepository
import com.cook.easypan.easypan.domain.repository.UserRepository
import com.cook.easypan.easypan.presentation.authentication.AuthenticationRoot
import com.cook.easypan.easypan.presentation.authentication.AuthenticationViewModel
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EasyPanUiTest {

    @get:Rule
    val rule = createComposeRule()

    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: AuthenticationViewModel
    private lateinit var authClient: AuthClient

    @Before
    fun setUp() {
        authClient = mockk(relaxed = true)
        val firestoreDataSource = mockk<FirestoreClient>(relaxed = true)
        userRepository = DefaultUserRepository(firestoreDataSource, authClient)
        viewModel = AuthenticationViewModel(userRepository)
    }

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.cook.easypan", appContext.packageName)
    }

    @Suppress("UnusedFlow")
    @Test
    fun testAuthenticationScreen() {
        rule.setContent {
            AuthenticationRoot(
                viewModel = viewModel,
            )
        }
        rule.onNodeWithText("Continue with Google").performClick()
        rule.waitForIdle()
        verify(timeout = 2000) {
            authClient.signInWithGoogle(any())
        }
    }
}