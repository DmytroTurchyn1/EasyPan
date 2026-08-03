/*
 * Created  21/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.core.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.net.toUri
import com.cook.easypan.services.CountdownTimerService

object Launcher {
    private const val TAG = "Launcher"

    fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "No activity found to open app settings", e)
        }
    }

    fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "No activity found to open url", e)
        }
    }

    /**
     * Opens the system share sheet for an image [uri], which must come from the app's FileProvider.
     * Returns false when no app can handle the intent.
     */
    fun shareImage(context: Context, uri: Uri, chooserTitle: String): Boolean {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return try {
            context.startActivity(Intent.createChooser(intent, chooserTitle))
            true
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "No activity found to share an image", e)
            false
        }
    }

    fun startTimerService(context: Context, timerDuration: Long, ownerStep: Int) {
        val intent = Intent(context, CountdownTimerService::class.java).apply {
            action = CountdownTimerService.Actions.START.name
            putExtra(CountdownTimerService.EXTRA_DURATION_MS, timerDuration)
            putExtra(CountdownTimerService.EXTRA_OWNER_STEP, ownerStep)
        }
        context.startForegroundService(intent)
    }

    fun stopTimerService(context: Context) {
        val intent = Intent(context, CountdownTimerService::class.java).apply {
            action = CountdownTimerService.Actions.STOP.name
        }
        context.startService(intent)
    }

    fun pauseTimerService(context: Context) {
        val intent = Intent(context, CountdownTimerService::class.java).apply {
            action = CountdownTimerService.Actions.PAUSE.name
        }
        context.startForegroundService(intent)
    }
}
