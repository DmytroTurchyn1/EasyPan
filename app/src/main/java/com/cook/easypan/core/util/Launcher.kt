/*
 * Created  21/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.net.toUri
import com.cook.easypan.services.CountdownTimerService

object Launcher {
    fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        context.startActivity(intent)
    }

    fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        context.startActivity(intent)
    }

    fun startTimerService(context: Context, timerDuration: Long) {
        val intent = Intent(context, CountdownTimerService::class.java).apply {
            action = CountdownTimerService.Actions.START.name
            putExtra(CountdownTimerService.EXTRA_DURATION_MS, timerDuration)
        }
        context.startForegroundService(intent)
    }

    fun stopTimerService(context: Context) {
        val intent = Intent(context, CountdownTimerService::class.java).apply {
            action = CountdownTimerService.Actions.STOP.name
        }
        context.startForegroundService(intent)
    }

    fun pauseTimerService(context: Context) {
        val intent = Intent(context, CountdownTimerService::class.java).apply {
            action = CountdownTimerService.Actions.PAUSE.name
        }
        context.startForegroundService(intent)
    }
}