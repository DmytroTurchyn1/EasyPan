package com.dmytro_turchyn.easypan.di

import com.dmytro_turchyn.easypan.easypan.data.GoogleAuthUiClient
import com.dmytro_turchyn.easypan.easypan.presentation.authentication.AuthenticationViewModel
import com.google.android.gms.auth.api.identity.Identity
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module{
    single {
        GoogleAuthUiClient(
            context = androidContext(),
            oneTapClient = Identity.getSignInClient(androidContext())
        )
    }
    viewModelOf(::AuthenticationViewModel)
}