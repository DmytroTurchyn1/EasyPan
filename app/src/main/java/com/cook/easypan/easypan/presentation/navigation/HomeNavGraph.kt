package com.cook.easypan.easypan.presentation.navigation

import ProfileAction
import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cook.easypan.easypan.data.auth.GoogleAuthUiClient
import com.cook.easypan.easypan.presentation.SelectedRecipeViewModel
import com.cook.easypan.easypan.presentation.favorite.FavoriteRoot
import com.cook.easypan.easypan.presentation.favorite.FavoriteViewModel
import com.cook.easypan.easypan.presentation.home.HomeRoot
import com.cook.easypan.easypan.presentation.home.HomeViewModel
import com.cook.easypan.easypan.presentation.profile.ProfileRoot
import com.cook.easypan.easypan.presentation.profile.ProfileViewModel
import com.cook.easypan.easypan.presentation.recipe_detail.RecipeDetailAction
import com.cook.easypan.easypan.presentation.recipe_detail.RecipeDetailRoot
import com.cook.easypan.easypan.presentation.recipe_detail.RecipeDetailViewModel
import com.cook.easypan.easypan.presentation.recipe_finish.RecipeFinishRoot
import com.cook.easypan.easypan.presentation.recipe_finish.RecipeFinishViewModel
import com.cook.easypan.easypan.presentation.recipe_step.RecipeStepAction
import com.cook.easypan.easypan.presentation.recipe_step.RecipeStepRoot
import com.cook.easypan.easypan.presentation.recipe_step.RecipeStepViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun Home(
    googleAuthUiClient: GoogleAuthUiClient,
    navController: NavHostController = rememberNavController(),
    onSignOut: () -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            HomeNavGraph(
                navController = navController,
                googleAuthUiClient = googleAuthUiClient,
                onSignOut = onSignOut
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.Favorite,
        BottomBarScreen.Profile
    )
    val bottomBarDestination =
        screens.any { it.route::class.qualifiedName == currentDestination?.route }
    if (bottomBarDestination) {
        NavigationBar {
            screens.forEach { screen ->
                AddItem(
                    screen = screen,
                    currentDestination = currentDestination,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun HomeNavGraph(
    navController: NavHostController,
    googleAuthUiClient: GoogleAuthUiClient,
    onSignOut: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home
    ) {
        composable<Route.Home> {
            val viewModel = koinViewModel<HomeViewModel>()
            val selectedRecipeViewModel =
                it.sharedKoinViewModel<SelectedRecipeViewModel>(navController)

            LaunchedEffect(Unit) {
                selectedRecipeViewModel.onSelectRecipe(null)
            }

            HomeRoot(
                viewModel = viewModel,
                onRecipeClick = { recipe ->
                    selectedRecipeViewModel.onSelectRecipe(recipe)
                    navController.navigate(
                        Route.RecipeDetail(recipe.id)
                    )

                }
            )
        }
        composable<Route.RecipeDetail> {
            val selectedRecipeViewModel =
                it.sharedKoinViewModel<SelectedRecipeViewModel>(navController)
            val viewModel = koinViewModel<RecipeDetailViewModel>()
            val selectedRecipe by selectedRecipeViewModel.selectedRecipe.collectAsStateWithLifecycle()
            LaunchedEffect(selectedRecipe) {
                selectedRecipe?.let {
                    viewModel.onAction(RecipeDetailAction.OnRecipeChange(it))
                }
            }
            RecipeDetailRoot(
                viewModel = viewModel,
                onBackClick = {
                    navController.navigateUp()
                },
                onStartClick = {
                    navController.navigate(Route.RecipeStep)
                }
            )
        }
        composable<Route.RecipeStep> {
            val viewModel = koinViewModel<RecipeStepViewModel>()
            val selectedRecipeViewModel =
                it.sharedKoinViewModel<SelectedRecipeViewModel>(navController)
            val selectedRecipe by selectedRecipeViewModel.selectedRecipe.collectAsStateWithLifecycle()
            LaunchedEffect(selectedRecipe) {
                selectedRecipe?.let {
                    viewModel.onAction(RecipeStepAction.OnRecipeChange(it))
                }
            }
            RecipeStepRoot(
                viewModel = viewModel,
                onFinishClick = {
                    navController.popBackStack()
                    navController.navigate(Route.RecipeFinish)
                }
            )
        }

        composable<Route.RecipeFinish> {
            val viewModel = RecipeFinishViewModel()
            RecipeFinishRoot(
                viewModel = viewModel,
                onFinishClick = {
                    navController.navigate(Route.Home)
                }
            )
        }

        composable<Route.Profile> {
            val viewModel = remember { ProfileViewModel() }
            LaunchedEffect(Unit) {
                viewModel.state.collect { event ->
                    if (event.isSignedOut) {
                        googleAuthUiClient.signOut()
                        onSignOut()
                    }
                }
            }
            ProfileRoot(
                userData = googleAuthUiClient.getSignedInUser(),
                onSignOutButton = {
                    viewModel.onAction(ProfileAction.OnSignOut)
                },
                viewModel = viewModel
            )
        }
        composable<Route.Favorite> {
            val viewModel = FavoriteViewModel()
            FavoriteRoot(
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomBarScreen,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
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
        selected = currentDestination?.route == screen.route::class.qualifiedName,
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        },
        label = { Text(text = stringResource(screen.title) ) },
        icon = {
            Icon(
                imageVector = screen.icon,
                contentDescription = "${screen.title} icon",
            )
        }
    )
}

@Composable
private inline fun <reified T : ViewModel> NavBackStackEntry.sharedKoinViewModel(
    navController: NavController
): T {
    // Use the Home route as the shared scope since it's your start destination
    val parentEntry = remember(this) {
        try {
            navController.getBackStackEntry(Route.Home::class.qualifiedName!!)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, e.toString())
            navController.currentBackStackEntry!!
        }
    }
    return koinViewModel(viewModelStoreOwner = parentEntry)
}