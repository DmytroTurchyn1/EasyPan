package com.cook.easypan.di


import com.cook.easypan.easypan.data.auth.AuthClient
import com.cook.easypan.easypan.data.database.FirestoreClient
import com.cook.easypan.easypan.data.repository.DefaultRecipeRepository
import com.cook.easypan.easypan.data.repository.DefaultUserRepository
import com.cook.easypan.easypan.domain.repository.RecipeRepository
import com.cook.easypan.easypan.domain.repository.UserRepository
import com.cook.easypan.easypan.presentation.SelectedRecipeViewModel
import com.cook.easypan.easypan.presentation.authentication.AuthenticationViewModel
import com.cook.easypan.easypan.presentation.home.HomeViewModel
import com.cook.easypan.easypan.presentation.profile.ProfileViewModel
import com.cook.easypan.easypan.presentation.recipe_detail.RecipeDetailViewModel
import com.cook.easypan.easypan.presentation.recipe_finish.RecipeFinishViewModel
import com.cook.easypan.easypan.presentation.recipe_step.RecipeStepViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single {
        AuthClient(
            applicationContext = androidContext(),
        )
    }

    single<UserRepository> {
        DefaultUserRepository(
            firestoreDataSource = get(),
            googleAuthClient = get()
        )
    }

    singleOf(::FirestoreClient)

    singleOf(::DefaultRecipeRepository).bind<RecipeRepository>()

    viewModelOf(::AuthenticationViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::RecipeDetailViewModel)
    viewModelOf(::SelectedRecipeViewModel)
    viewModelOf(::RecipeStepViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::RecipeFinishViewModel)
}