package com.dmytro_turchyn.easypan.di


import com.dmytro_turchyn.easypan.easypan.data.auth.GoogleAuthUiClient
import com.dmytro_turchyn.easypan.easypan.data.database.FirestoreClient
import com.dmytro_turchyn.easypan.easypan.data.repository.DefaultRecipeRepository
import com.dmytro_turchyn.easypan.easypan.domain.RecipeRepository
import com.dmytro_turchyn.easypan.easypan.presentation.authentication.AuthenticationViewModel
import com.dmytro_turchyn.easypan.easypan.presentation.home.HomeViewModel
import com.google.android.gms.auth.api.identity.Identity
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module{
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
}