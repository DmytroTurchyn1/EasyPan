/*
 * Created  22/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cook.easypan.easypan.domain.repository.BillingRepository
import com.cook.easypan.easypan.presentation.SelectedPlanViewModel
import com.cook.easypan.easypan.presentation.SelectedRecipeViewModel
import com.cook.easypan.easypan.presentation.favorite.FavoriteRoot
import com.cook.easypan.easypan.presentation.favorite.FavoriteViewModel
import com.cook.easypan.easypan.presentation.home.HomeRoot
import com.cook.easypan.easypan.presentation.home.HomeViewModel
import com.cook.easypan.easypan.presentation.ingredients_receipt.IngredientsReceiptAction
import com.cook.easypan.easypan.presentation.ingredients_receipt.IngredientsReceiptRoot
import com.cook.easypan.easypan.presentation.ingredients_receipt.IngredientsReceiptViewModel
import com.cook.easypan.easypan.presentation.meal_plan.MealPlanAction
import com.cook.easypan.easypan.presentation.meal_plan.MealPlanRoot
import com.cook.easypan.easypan.presentation.meal_plan.MealPlanViewModel
import com.cook.easypan.easypan.presentation.meal_plan_review.MealPlanReviewAction
import com.cook.easypan.easypan.presentation.meal_plan_review.MealPlanReviewRoot
import com.cook.easypan.easypan.presentation.meal_plan_review.MealPlanReviewViewModel
import com.cook.easypan.easypan.presentation.meal_plan_wizard.MealPlanWizardRoot
import com.cook.easypan.easypan.presentation.meal_plan_wizard.MealPlanWizardViewModel
import com.cook.easypan.easypan.presentation.paywall.PaywallRoot
import com.cook.easypan.easypan.presentation.profile.ProfileRoot
import com.cook.easypan.easypan.presentation.profile.ProfileViewModel
import com.cook.easypan.easypan.presentation.recipe_detail.RecipeDetailAction
import com.cook.easypan.easypan.presentation.recipe_detail.RecipeDetailRoot
import com.cook.easypan.easypan.presentation.recipe_detail.RecipeDetailViewModel
import com.cook.easypan.easypan.presentation.recipe_finish.RecipeFinishAction
import com.cook.easypan.easypan.presentation.recipe_finish.RecipeFinishRoot
import com.cook.easypan.easypan.presentation.recipe_finish.RecipeFinishViewModel
import com.cook.easypan.easypan.presentation.recipe_step.RecipeStepAction
import com.cook.easypan.easypan.presentation.recipe_step.RecipeStepRoot
import com.cook.easypan.easypan.presentation.recipe_step.RecipeStepViewModel
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject


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
        startDestination = Route.Home,
        modifier = Modifier
            .semantics {
                testTagsAsResourceId = true
            }
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
                selectedRecipe?.let { recipe ->
                    viewModel.onAction(RecipeDetailAction.OnRecipeChange(recipe))
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
            val state by viewModel.state.collectAsStateWithLifecycle()
            LaunchedEffect(selectedRecipe) {
                selectedRecipe?.let { recipe ->
                    viewModel.onAction(RecipeStepAction.OnRecipeChange(recipe))
                }
            }
            LaunchedEffect(state.isFinishButtonEnabled) {
                if (!state.isFinishButtonEnabled) {
                    navController.navigate(
                        Route.RecipeFinish(
                            state.recipe?.id ?: throw IllegalStateException("Recipe not found")
                        )
                    ) {
                        popUpTo(Route.RecipeStep) {
                            inclusive = true
                        }
                    }
                }
            }
            RecipeStepRoot(
                viewModel = viewModel,
                onCancelClick = {
                    navController.navigateUp()
                }
            )
        }

        composable<Route.RecipeFinish> {
            val viewModel = koinViewModel<RecipeFinishViewModel>()
            val selectedRecipeViewModel =
                it.sharedKoinViewModel<SelectedRecipeViewModel>(navController)
            val selectedRecipe by selectedRecipeViewModel.selectedRecipe.collectAsStateWithLifecycle()
            LaunchedEffect(selectedRecipe) {
                selectedRecipe?.let { recipe ->
                    viewModel.onAction(RecipeFinishAction.OnRecipeChange(recipe))
                }
            }
            RecipeFinishRoot(
                viewModel = viewModel,
                onFinishClick = {
                    navController.navigate(Route.Home) {
                        popUpTo(Route.Home) {
                            inclusive = true
                        }
                        launchSingleTop = true
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
        composable<Route.MealPlan> {
            val viewModel = koinViewModel<MealPlanViewModel>()
            val selectedPlanViewModel =
                it.sharedKoinViewModel<SelectedPlanViewModel>(navController)
            val selectedRecipeViewModel =
                it.sharedKoinViewModel<SelectedRecipeViewModel>(navController)
            val preferences by selectedPlanViewModel.preferences.collectAsStateWithLifecycle()

            LaunchedEffect(preferences) {
                preferences?.let { prefs ->
                    viewModel.onAction(MealPlanAction.OnGenerate(prefs))
                }
            }

            val isChef by koinInject<BillingRepository>().isChef.collectAsStateWithLifecycle()
            if (isChef) {
                MealPlanRoot(
                    viewModel = viewModel,
                    onOpenWizard = {
                        navController.navigate(Route.MealPlanWizard)
                    },
                    onRecipeClick = { recipe ->
                        selectedRecipeViewModel.onSelectRecipe(recipe)
                        navController.navigate(Route.RecipeDetail(recipe.id))
                    },
                )
            } else {
                PaywallRoot()
            }
        }
        composable<Route.IngredientsReceipt> {
            val viewModel = koinViewModel<IngredientsReceiptViewModel>()
            val selectedPlanViewModel =
                it.sharedKoinViewModel<SelectedPlanViewModel>(navController)
            val preferences by selectedPlanViewModel.preferences.collectAsStateWithLifecycle()

            LaunchedEffect(preferences) {
                preferences?.let { prefs ->
                    viewModel.onAction(IngredientsReceiptAction.OnGenerate(prefs))
                }
            }

            val isChef by koinInject<BillingRepository>().isChef.collectAsStateWithLifecycle()
            if (isChef) {
                IngredientsReceiptRoot(
                    viewModel = viewModel
                )
            } else {
                PaywallRoot()
            }
        }
        composable<Route.MealPlanReview> {
            val viewModel = koinViewModel<MealPlanReviewViewModel>()
            val selectedPlanViewModel =
                it.sharedKoinViewModel<SelectedPlanViewModel>(navController)
            val selectedRecipeViewModel =
                it.sharedKoinViewModel<SelectedRecipeViewModel>(navController)
            val preferences by selectedPlanViewModel.preferences.collectAsStateWithLifecycle()

            LaunchedEffect(preferences) {
                preferences?.let { prefs ->
                    viewModel.onAction(MealPlanReviewAction.OnGenerate(prefs))
                }
            }

            MealPlanReviewRoot(
                viewModel = viewModel,
                onDismiss = {
                    navController.navigate(Route.MealPlan) {
                        popUpTo(Route.MealPlan) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onEdit = {
                    navController.navigate(Route.MealPlanWizard) {
                        popUpTo(Route.MealPlanReview) { inclusive = true }
                    }
                },
                onContinue = {
                    val showReceiptScreen: Boolean =
                        Firebase.remoteConfig.getBoolean("receipt_screen")
                    if (showReceiptScreen) {
                        navController.navigate(Route.IngredientsReceipt) {
                            popUpTo(Route.MealPlanReview) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Route.MealPlan) {
                            popUpTo(Route.MealPlanReview) { inclusive = true }
                        }
                    }
                },
                onRecipeClick = { recipe ->
                    selectedRecipeViewModel.onSelectRecipe(recipe)
                    navController.navigate(Route.RecipeDetail(recipe.id))
                },
            )
        }
        composable<Route.MealPlanWizard> {
            val viewModel = koinViewModel<MealPlanWizardViewModel>()
            val selectedPlanViewModel =
                it.sharedKoinViewModel<SelectedPlanViewModel>(navController)
            MealPlanWizardRoot(
                viewModel = viewModel,
                onExit = {
                    navController.navigateUp()
                },
                onFinish = { preferences ->
                    selectedPlanViewModel.onPlanRequested(preferences)
                    navController.navigate(Route.MealPlanReview) {
                        popUpTo(Route.MealPlanWizard) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable<Route.Favorite> {
            val viewModel = koinViewModel<FavoriteViewModel>()
            val selectedRecipeViewModel =
                it.sharedKoinViewModel<SelectedRecipeViewModel>(navController)
            LaunchedEffect(Unit) {
                selectedRecipeViewModel.onSelectRecipe(null)
            }

            FavoriteRoot(
                viewModel = viewModel,
                onRecipeClick = { recipe ->
                    selectedRecipeViewModel.onSelectRecipe(recipe)
                    navController.navigate(Route.RecipeDetail(recipe.id))
                }
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
            Log.e("HomeNavGraph", e.toString())
            navController.currentBackStackEntry!!
        }
    }
    return koinViewModel(viewModelStoreOwner = parentEntry)
}