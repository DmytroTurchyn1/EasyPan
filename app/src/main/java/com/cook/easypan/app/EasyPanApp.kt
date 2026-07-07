/*
 * Created  8/9/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import com.cook.easypan.core.util.CHANNEL_ID_FIREBASE
import com.cook.easypan.core.util.CHANNEL_ID_TIMER_SERVICE
import com.cook.easypan.core.util.CHANNEL_NAME_FIREBASE
import com.cook.easypan.core.util.CHANNEL_NAME_TIMER_SERVICE
import com.cook.easypan.di.appModule
import com.cook.easypan.easypan.data.datastore.AppSettings
import com.cook.easypan.easypan.data.datastore.AppSettingsSerializer
import com.google.firebase.Firebase
import com.google.firebase.initialize
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

val Context.dataStore by dataStore(
    fileName = "settings.json",
    serializer = AppSettingsSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { AppSettings() }
)

class EasyPanApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@EasyPanApp)
            androidLogger()
            modules(appModule)
        }

        Firebase.initialize(this)
        AppCheckInstaller.install()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID_FIREBASE,
                CHANNEL_NAME_FIREBASE,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID_TIMER_SERVICE,
                CHANNEL_NAME_TIMER_SERVICE,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }
}
