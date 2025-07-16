package com.cook.easypan.app

import android.app.Application
import com.cook.easypan.di.appModule
import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class EasyPanApp : Application() {
    /**
     * Initializes application-wide dependencies and services on startup.
     *
     * Sets up Koin dependency injection, initializes Firebase, and configures Firebase App Check with the debug provider factory.
     */
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@EasyPanApp)
            androidLogger()
            modules(appModule)
        }
        Firebase.initialize(this)
        val buildType = BuildConfig.BUILD_TYPE.contentEquals("debug")
        if (true) {
            Firebase.appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        }


    }
}