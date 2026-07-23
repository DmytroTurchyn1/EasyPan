/*
 * Created  14/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.di


import com.cook.easypan.easypan.data.auth.AuthClient
import com.cook.easypan.easypan.data.database.FirestoreClient
import com.cook.easypan.easypan.data.repository.DefaultRecipeRepository
import com.cook.easypan.easypan.data.repository.DefaultUserRepository
import com.cook.easypan.easypan.domain.repository.RecipeRepository
import com.cook.easypan.easypan.domain.repository.UserRepository
import com.cook.easypan.easypan.domain.usecase.BuildGroceriesListUseCase
import com.cook.easypan.easypan.domain.usecase.GenerateMealPlanUseCase
import com.cook.easypan.easypan.presentation.SelectedPlanViewModel
import com.cook.easypan.easypan.presentation.SelectedRecipeViewModel
import com.cook.easypan.easypan.presentation.authentication.AuthenticationViewModel
import com.cook.easypan.easypan.presentation.favorite.FavoriteViewModel
import com.cook.easypan.easypan.presentation.home.HomeViewModel
import com.cook.easypan.easypan.presentation.ingredients_receipt.IngredientsReceiptViewModel
import com.cook.easypan.easypan.presentation.meal_plan.MealPlanViewModel
import com.cook.easypan.easypan.presentation.meal_plan_review.MealPlanReviewViewModel
import com.cook.easypan.easypan.presentation.meal_plan_wizard.MealPlanWizardViewModel
import com.cook.easypan.easypan.presentation.profile.ProfileViewModel
import com.cook.easypan.easypan.presentation.recipe_detail.RecipeDetailViewModel
import com.cook.easypan.easypan.presentation.recipe_finish.RecipeFinishViewModel
import com.cook.easypan.easypan.presentation.recipe_step.RecipeStepViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {

    single<FirebaseAuth> { Firebase.auth }
    single<FirebaseFirestore> { Firebase.firestore }


    singleOf(::FirestoreClient)
    singleOf(::AuthClient)
    singleOf(::DefaultRecipeRepository).bind<RecipeRepository>()
    singleOf(::DefaultUserRepository).bind<UserRepository>()
    singleOf(::GenerateMealPlanUseCase)
    singleOf(::BuildGroceriesListUseCase)

    viewModelOf(::AuthenticationViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::MealPlanViewModel)
    viewModelOf(::MealPlanWizardViewModel)
    viewModelOf(::MealPlanReviewViewModel)
    viewModelOf(::IngredientsReceiptViewModel)
    viewModelOf(::RecipeDetailViewModel)
    viewModelOf(::SelectedRecipeViewModel)
    viewModelOf(::SelectedPlanViewModel)
    viewModelOf(::RecipeStepViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::RecipeFinishViewModel)
    viewModelOf(::FavoriteViewModel)
}