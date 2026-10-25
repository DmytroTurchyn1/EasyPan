package com.cook.easypan.easypan.data.notification

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.cook.easypan.core.domain.NotificationPermissionChecker
import com.cook.easypan.core.util.NOTIFICATION_TOPIC
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging

class AndroidNotificationPermissionChecker(
    private val context: Context,
) : NotificationPermissionChecker {

    override val isRuntimePermissionRequired: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    override fun isGranted(): Boolean {
        return if (isRuntimePermissionRequired) {
            checkSelfPermission(context, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    override fun subscribeToTopicIfGranted() {
        if (!isGranted()) return
        Firebase.messaging.subscribeToTopic(NOTIFICATION_TOPIC)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e(
                        "NotificationPermission",
                        "Failed to subscribe to topic: ${task.exception?.message}"
                    )
                }
            }
    }
}
