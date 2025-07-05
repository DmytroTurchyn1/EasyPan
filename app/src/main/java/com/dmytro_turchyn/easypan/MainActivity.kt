package com.dmytro_turchyn.easypan

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.dmytro_turchyn.easypan.easypan.data.GoogleAuthUiClient
import com.dmytro_turchyn.easypan.easypan.presentation.authentication.AuthenticationRoot
import com.dmytro_turchyn.easypan.easypan.presentation.authentication.AuthenticationViewModel
import com.dmytro_turchyn.easypan.easypan.presentation.home.HomeRoot
import com.dmytro_turchyn.easypan.easypan.presentation.home.HomeViewModel
import com.dmytro_turchyn.easypan.ui.theme.EasyPanTheme
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasyPanTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Route.AppGraph
                ){
                   navigation<Route.AppGraph>(
                       startDestination = if (googleAuthUiClient.getSignedInUser() != null ) Route.Home else Route.Authentication
                   ) {
                       composable<Route.Authentication> {
                           val viewModel = koinViewModel<AuthenticationViewModel>()
                           val state by viewModel.state.collectAsStateWithLifecycle()

                           val launcher = rememberLauncherForActivityResult(
                               contract = ActivityResultContracts.StartIntentSenderForResult(),
                               onResult = {result ->
                                   if (result.resultCode == RESULT_OK){
                                       lifecycleScope.launch{
                                           val signInResult = googleAuthUiClient.signInWithIntent(result.data ?: return@launch)
                                           viewModel.onLoginResult(signInResult)
                                       }
                                   }
                               }
                           )

                           LaunchedEffect(Unit) {
                               if(googleAuthUiClient.getSignedInUser() != null) {
                                   navController.navigate("chat")
                               }
                           }
                           LaunchedEffect(state.signInIntentSender) {
                               launcher.launch(
                                   IntentSenderRequest.Builder(
                                       state.signInIntentSender ?: return@LaunchedEffect
                                   ).build()
                               )
                           }

                           LaunchedEffect(key1 = state.isSignInSuccessful) {
                               if(state.isSignInSuccessful) {
                                   Toast.makeText(
                                       applicationContext,
                                       "Sign in successful",
                                       Toast.LENGTH_LONG
                                   ).show()

                                   navController.navigate("chat")
                                   viewModel.resetState()
                               }
                           }

                           AuthenticationRoot(
                               viewModel = viewModel
                           )
                       }
                       composable<Route.Home> {
                           HomeRoot(
                               viewModel = HomeViewModel()
                           )
                       }
                   }
                }

            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EasyPanTheme {

    }
}