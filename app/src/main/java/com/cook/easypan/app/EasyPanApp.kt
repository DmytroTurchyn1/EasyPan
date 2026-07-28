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
import android.util.Log
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import com.cook.easypan.BuildConfig
import com.cook.easypan.R
import com.cook.easypan.core.util.CHANNEL_ID_FIREBASE
import com.cook.easypan.core.util.CHANNEL_ID_TIMER_SERVICE
import com.cook.easypan.core.util.CHANNEL_NAME_FIREBASE
import com.cook.easypan.core.util.CHANNEL_NAME_TIMER_SERVICE
import com.cook.easypan.di.appModule
import com.cook.easypan.easypan.data.datastore.AppSettings
import com.cook.easypan.easypan.data.datastore.AppSettingsSerializer
import com.cook.easypan.easypan.domain.repository.BillingRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.initialize
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import org.koin.android.ext.android.get
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
        configureRevenueCat()
        createNotificationChannels()
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(
                    "RemoteConfig",
                    "activated=${task.result}, receipt_screen=${remoteConfig.getBoolean("receipt_screen")}"
                )
            } else {
                Log.e("RemoteConfig", "fetch failed", task.exception)
            }
        }
    }

    /**
     * RevenueCat must be configured once per process, before any paywall or entitlement check runs.
     * Doing it here rather than in [MainActivity] avoids re-configuring on every activity
     * recreation (rotation, theme change).
     */
    private fun configureRevenueCat() {
        if (BuildConfig.REVENUECAT_API_KEY.isBlank()) {
            Log.e(
                "RevenueCat",
                "REVENUECAT_API_KEY is blank — add revenueCatApiKey to keystore.properties. " +
                        "Paywalls and entitlement checks will be inactive."
            )
        } else {
            Purchases.logLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.ERROR
            Purchases.configure(
                PurchasesConfiguration.Builder(this, BuildConfig.REVENUECAT_API_KEY)
                    // Cold start with an existing session: bill as the signed-in user, not anonymous.
                    .appUserID(Firebase.auth.currentUser?.uid)
                    .build()
            )
        }
        // Always start observing: on a blank key this unlocks premium features instead of
        // gating everyone out of a misconfigured dev build.
        get<BillingRepository>().startObserving()
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
