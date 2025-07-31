package com.cook.easypan

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cook.easypan.easypan.data.auth.GoogleAuthUiClient
import com.cook.easypan.easypan.data.database.FirestoreClient
import com.cook.easypan.easypan.data.repository.DefaultUserRepository
import com.cook.easypan.easypan.domain.UserRepository
import com.cook.easypan.easypan.presentation.authentication.AuthenticationRoot
import com.cook.easypan.easypan.presentation.authentication.AuthenticationViewModel
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EasyPanUiTest {

    @get:Rule
    val rule = createComposeRule()

    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: AuthenticationViewModel
    private lateinit var googleAuthUiClient: GoogleAuthUiClient

    @Before
    fun setUp() {
        googleAuthUiClient = mockk(relaxed = true)
        val firestoreDataSource = mockk<FirestoreClient>(relaxed = true)
        userRepository = DefaultUserRepository(firestoreDataSource, googleAuthUiClient)
        viewModel = AuthenticationViewModel(userRepository)
    }

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
            googleAuthUiClient.signIn()
        }
    }
}