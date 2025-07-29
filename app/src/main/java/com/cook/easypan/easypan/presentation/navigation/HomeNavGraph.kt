package com.cook.easypan.easypan.presentation.navigation

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                onSignOut = onSignOut
            )
        }
    }
}

@Composable
fun HomeNavGraph(
    navController: NavHostController,
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
                    navController.navigate(Route.RecipeDetail(recipe.id))
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
                    navController.navigate(Route.RecipeFinish) {
                        popUpTo(Route.RecipeStep) {
                            inclusive = true
                        }
                    }
                },
                onCancelClick = {
                    navController.navigateUp()
                }
            )
        }

        composable<Route.RecipeFinish> {
            val viewModel = koinViewModel<RecipeFinishViewModel>()
            RecipeFinishRoot(
                viewModel = viewModel,
                onFinishClick = {
                    navController.navigate(Route.Home) {
                        popUpTo(Route.RecipeFinish) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<Route.Profile> {
            val viewModel = koinViewModel<ProfileViewModel>()

            ProfileRoot(
                onSignOutButton = { onSignOut() },
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