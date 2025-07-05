package com.dmytro_turchyn.easypan

import android.app.Application
import com.dmytro_turchyn.easypan.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class EasyPanApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin{
            androidContext(this@EasyPanApp)
            androidLogger()
            modules(appModule)
        }
    }
}