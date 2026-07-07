/*
 * Created  8/9/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.services

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.cook.easypan.R
import com.cook.easypan.app.MainActivity
import com.cook.easypan.core.CountdownTimer
import com.cook.easypan.core.util.CHANNEL_ID_TIMER_SERVICE
import com.cook.easypan.core.util.formatMsToMS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class CountdownTimerService : Service() {

    companion object {
        const val EXTRA_DURATION_MS = "extra_duration_ms"
        const val EXTRA_OWNER_STEP = "extra_owner_step"
        private const val ONGOING_NOTIFICATION_ID = 1
        private const val FINISHED_NOTIFICATION_ID = 2
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "${packageName}:CountdownTimer"
        )
        listenToTimer()
    }

    private fun listenToTimer() {
        scope.launch {
            CountdownTimer.remainingSeconds.collect { remainingSeconds ->
                when {
                    remainingSeconds == null -> Unit
                    remainingSeconds > 0L -> {
                        if (CountdownTimer.isRunning.value) {
                            updateNotification(formatMsToMS(remainingSeconds * 1000L))
                        }
                    }

                    else -> onTimerFinished()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.name -> {
                val duration = intent.getLongExtra(EXTRA_DURATION_MS, -1L)
                val ownerStep = intent.getIntExtra(EXTRA_OWNER_STEP, -1).takeIf { it >= 0 }
                if (duration > 0L) {
                    start(duration, ownerStep)
                } else {
                    stop()
                }
            }

            Actions.PAUSE.name -> pause()
            Actions.STOP.name -> stop()
            // Restarted by the system with a null intent: the in-memory timer
            // state is gone, so shut down instead of lingering.
            else -> stop()
        }
        return START_NOT_STICKY
    }

    private fun buildNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID_TIMER_SERVICE)
            .setSmallIcon(R.drawable.notification_ic)
            .setContentTitle(getString(R.string.countdown_timer_notification_title))
            .setContentText(contentText)
            .setContentIntent(contentIntent())
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    private fun start(requestedDurationMs: Long, ownerStep: Int?) {
        // Every startForegroundService() call must be answered with
        // startForeground(); it also promotes/updates the notification.
        startForeground(
            ONGOING_NOTIFICATION_ID,
            buildNotification(formatMsToMS(requestedDurationMs))
        )
        // A START replaces any running countdown, so the wake lock must be
        // resized to the new duration (small buffer so it survives until the
        // finished notification is posted).
        if (wakeLock.isHeld) wakeLock.release()
        wakeLock.acquire(requestedDurationMs + 5_000L)
        CountdownTimer.startGlobal(requestedDurationMs, ownerStep)
    }

    private fun pause() {
        CountdownTimer.pause()
        if (wakeLock.isHeld) wakeLock.release()
        val remainingSeconds = CountdownTimer.remainingSeconds.value
        if (remainingSeconds == null) {
            // Nothing to pause; satisfy the foreground contract, then stop.
            startForeground(
                ONGOING_NOTIFICATION_ID,
                buildNotification(getString(R.string.timer_paused))
            )
            stop()
            return
        }
        startForeground(
            ONGOING_NOTIFICATION_ID,
            buildNotification(
                "${getString(R.string.timer_paused)} · ${formatMsToMS(remainingSeconds * 1000L)}"
            )
        )
    }

    private fun stop() {
        CountdownTimer.stop()
        if (wakeLock.isHeld) wakeLock.release()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun onTimerFinished() {
        if (wakeLock.isHeld) wakeLock.release()
        showFinishedNotification()
        CountdownTimer.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun showFinishedNotification() {
        if (!canPostNotifications()) return
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_TIMER_SERVICE)
            .setSmallIcon(R.drawable.notification_ic)
            .setContentTitle(getString(R.string.timer_finished_notification_title))
            .setContentText(getString(R.string.countdown_timer_notification_finished))
            .setContentIntent(contentIntent())
            .setAutoCancel(true)
            .build()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(FINISHED_NOTIFICATION_ID, notification)
    }

    private fun updateNotification(contentText: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ONGOING_NOTIFICATION_ID, buildNotification(contentText))
    }

    private fun contentIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        scope.cancel()
        if (wakeLock.isHeld) wakeLock.release()
        super.onDestroy()
    }

    enum class Actions {
        START,
        PAUSE,
        STOP
    }
}
