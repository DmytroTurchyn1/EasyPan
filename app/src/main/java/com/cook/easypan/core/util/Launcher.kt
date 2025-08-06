package com.cook.easypan.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.net.toUri

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
}