package com.cook.easypan

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cook.easypan.easypan.data.auth.GoogleAuthUiClient
import com.cook.easypan.easypan.presentation.authentication.AuthenticationRoot
import com.cook.easypan.easypan.presentation.authentication.AuthenticationViewModel
import com.cook.easypan.easypan.presentation.favorite.FavoriteRoot
import com.cook.easypan.easypan.presentation.home.HomeRoot
import com.cook.easypan.easypan.presentation.profile.ProfileRoot
import com.cook.easypan.easypan.presentation.profile.ProfileViewModel
import com.cook.easypan.ui.theme.EasyPanTheme
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    private val bottomNavItems = listOf(
        BottomNavigationItem(
            title = "Home",
            unselectedIcon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home,
            route = AppGraph.Home
        ),
        BottomNavigationItem(
            title = "Favorite",
            unselectedIcon = Icons.Outlined.Favorite,
            selectedIcon = Icons.Filled.Favorite,
            route = AppGraph.Favorite
        ),
        BottomNavigationItem(
            title = "Profile",
            unselectedIcon = Icons.Outlined.Accessibility,
            selectedIcon = Icons.Filled.Accessibility,
            route = AppGraph.Profile
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasyPanTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Show bottom bar only for main app screens
                val showBottomBar = currentRoute in listOf(
                    AppGraph.Home::class.qualifiedName,
                    AppGraph.Favorite::class.qualifiedName,
                    AppGraph.Profile::class.qualifiedName
                )

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                bottomNavItems.forEach { item ->
                                    val isSelected = currentRoute == item.route::class.qualifiedName
                                    NavigationBarItem(
                                        colors = NavigationBarItemColors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            disabledIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                            selectedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ),
                                        selected = isSelected,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(AppGraph.Home) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        label = { Text(text = item.title) },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = "${item.title} icon",
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (googleAuthUiClient.getSignedInUser() == null) {
                            AuthGraph.Authentication
                        } else {
                            AppGraph.Home
                        },
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Authentication flow
                        composable<AuthGraph.Authentication> {
                            val viewModel = koinViewModel<AuthenticationViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()

                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiClient.signInWithIntent(
                                                result.data ?: return@launch
                                            )
                                            viewModel.onLoginResult(signInResult)
                                        }
                                    }
                                }
                            )

                            LaunchedEffect(state.signInIntentSender) {
                                state.signInIntentSender?.let { sender ->
                                    launcher.launch(IntentSenderRequest.Builder(sender).build())
                                }
                            }

                            LaunchedEffect(state.isSignInSuccessful) {
                                if (state.isSignInSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Sign in successful",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.navigate(AppGraph.Home) {
                                        popUpTo(AuthGraph.Authentication) { inclusive = true }
                                    }
                                    viewModel.resetState()
                                }
                            }

                            AuthenticationRoot(viewModel = viewModel)
                        }

                        // Main app screens
                        composable<AppGraph.Home> {
                            HomeRoot()
                        }

                        composable<AppGraph.Favorite> {
                            FavoriteRoot()
                        }

                        composable<AppGraph.Profile> {
                            val viewModel = ProfileViewModel()
                            lifecycleScope.launch {
                                viewModel.state.collect{ event ->
                                    if(event.isSignedOut){
                                            navController.navigate(AuthGraph.Authentication) {
                                                popUpTo(AppGraph.Home) { inclusive = true }
                                            }
                                    }
                                }
                            }
                            ProfileRoot(
                                userData = googleAuthUiClient.getSignedInUser(),
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}