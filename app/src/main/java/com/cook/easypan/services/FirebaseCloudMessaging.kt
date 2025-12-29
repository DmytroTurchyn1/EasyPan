/*
 * Created  21/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cook.easypan.R
import com.cook.easypan.app.MainActivity
import com.cook.easypan.core.util.CHANNEL_ID_FIREBASE
import com.cook.easypan.core.util.CHANNEL_NAME_FIREBASE
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class FirebaseCloudMessaging : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        message.notification?.let {
            showNotification(it)
        }

        if (message.data.isNotEmpty()) {
            handleDataMessage(message)
        }
    }

    private fun handleDataMessage(message: RemoteMessage) {
        Log.d("FirebaseCloudMessaging", "Data message received: ${message.data}")
    }

    private fun showNotification(message: RemoteMessage.Notification) {

        val channelId = CHANNEL_ID_FIREBASE

        val channelName = CHANNEL_NAME_FIREBASE

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification_ic)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)

        manager.createNotificationChannel(channel)

        manager.notify(Random.nextInt(), notificationBuilder)
    }
}