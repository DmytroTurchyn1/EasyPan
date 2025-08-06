package com.cook.easypan.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cook.easypan.easypan.presentation.navigation.RootNavGraph
import com.cook.easypan.ui.theme.EasyPanTheme
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        retrieveToken()
        setContent {
            EasyPanTheme {
                RootNavGraph()
            }
        }
    }

    private fun retrieveToken() {
        Firebase.messaging.token
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val token = it.result
                    Log.d("FirebaseCloudMessaging", "New token: $token")
                }
            }
    }

}