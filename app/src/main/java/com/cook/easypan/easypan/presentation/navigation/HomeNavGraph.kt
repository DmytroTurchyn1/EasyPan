package com.cook.easypan.easypan.presentation.navigation

import ProfileAction
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
import com.cook.easypan.easypan.presentation.home.HomeRoot
import com.cook.easypan.easypan.presentation.home.HomeViewModel
import com.cook.easypan.easypan.presentation.profile.ProfileRoot
import com.cook.easypan.easypan.presentation.profile.ProfileViewModel
import com.cook.easypan.easypan.presentation.recipe_detail.RecipeDetailAction
import com.cook.easypan.easypan.presentation.recipe_detail.RecipeDetailRoot
import com.cook.easypan.easypan.presentation.recipe_detail.RecipeDetailViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun Home(
    googleAuthUiClient : GoogleAuthUiClient,
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
        ){
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
        BottomBarScreen.Profile,
    )
    val bottomBarDestination = screens.any { it.route::class.qualifiedName == currentDestination?.route }
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
    googleAuthUiClient : GoogleAuthUiClient,
    onSignOut: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home
    ){
        composable<Route.Home> {
            val viewModel = koinViewModel<HomeViewModel>()
            val selectedRecipeViewModel = it.sharedKoinViewModel<SelectedRecipeViewModel>(navController)
            LaunchedEffect(true) {
                selectedRecipeViewModel.onSelectRecipe(null)
            }
            HomeRoot(
                viewModel = viewModel,
                onRecipeClick = { recipe ->
                    selectedRecipeViewModel.onSelectRecipe(recipe)
                    println("Selected recipe: ${recipe.id}")
                    navController.navigate(
                        Route.RecipeDetail(recipe.id)
                    )

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
                onSingOutButton = {
                    viewModel.onAction(ProfileAction.OnSignOut)
                },
                viewModel = viewModel
            )
        }
        composable<Route.Favorite> {
            FavoriteRoot()
        }
        composable<Route.RecipeDetail> {
            val selectedRecipeViewModel = it.sharedKoinViewModel<SelectedRecipeViewModel>(navController)
            val viewModel = koinViewModel<RecipeDetailViewModel>()
            val selectedRecipe by  selectedRecipeViewModel.selectedRecipe.collectAsStateWithLifecycle()
            println("recipe: Selected recipe mainActivity: ${selectedRecipe?.title}")
            LaunchedEffect(selectedRecipe) {
                println("recipe: Selected recipe changed: ${selectedRecipe?.title}")
                selectedRecipe?.let{
                    viewModel.onAction(RecipeDetailAction.OnRecipeChange(it))
                }
            }
            RecipeDetailRoot(
                viewModel = viewModel,
                onBackClick = {
                    navController.navigateUp()
                }
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
        label = { Text(text = screen.title) },
        icon = {
            Icon(
                imageVector = screen.icon,
                contentDescription = "${screen.title} icon",
            )
        }
    )
}

@Composable
private inline fun <reified T: ViewModel> NavBackStackEntry.sharedKoinViewModel(
    navController: NavController
): T {
    val navGraphRoute = destination.parent?.route ?: return koinViewModel<T>()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return koinViewModel(viewModelStoreOwner = parentEntry)
}