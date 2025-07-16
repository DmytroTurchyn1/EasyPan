package com.cook.easypan.di


import com.cook.easypan.easypan.data.auth.GoogleAuthUiClient
import com.cook.easypan.easypan.data.database.FirestoreClient
import com.cook.easypan.easypan.data.repository.DefaultRecipeRepository
import com.cook.easypan.easypan.domain.RecipeRepository
import com.cook.easypan.easypan.presentation.SelectedRecipeViewModel
import com.cook.easypan.easypan.presentation.authentication.AuthenticationViewModel
import com.cook.easypan.easypan.presentation.home.HomeViewModel
import com.cook.easypan.easypan.presentation.recipe_detail.RecipeDetailViewModel
import com.cook.easypan.easypan.presentation.recipe_step.RecipeStepViewModel
import com.google.android.gms.auth.api.identity.Identity
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single {
        GoogleAuthUiClient(
            context = androidContext(),
            oneTapClient = Identity.getSignInClient(androidContext())
        )
    }

    singleOf(::FirestoreClient)

    singleOf(::DefaultRecipeRepository).bind<RecipeRepository>()

    viewModelOf(::AuthenticationViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::RecipeDetailViewModel)
    viewModelOf(::SelectedRecipeViewModel)
    viewModelOf(::RecipeStepViewModel)
}