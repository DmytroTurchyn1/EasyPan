package com.cook.easypan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cook.easypan.easypan.data.auth.GoogleAuthUiClient
import com.cook.easypan.easypan.presentation.navigation.RootNavGraph
import com.cook.easypan.ui.theme.EasyPanTheme
import com.google.android.gms.auth.api.identity.Identity

class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }


    /**
     * Initializes the activity, sets up edge-to-edge display, and configures the Compose UI content with theming and navigation.
     *
     * @param savedInstanceState The previously saved instance state, or null if none exists.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasyPanTheme {
                RootNavGraph(
                    googleAuthUiClient = googleAuthUiClient
                )
            }
        }
    }
}