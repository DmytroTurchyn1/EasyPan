/*
 * Created  8/9/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cook.easypan.R
import com.cook.easypan.core.CountdownTimer
import com.cook.easypan.core.util.CHANNEL_ID_TIMER_SERVICE
import com.cook.easypan.core.util.CHANNEL_NAME_TIMER_SERVICE
import com.cook.easypan.core.util.formatMsToMS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
class CountdownTimerService : Service() {

    companion object {
        const val EXTRA_DURATION_MS = "extra_duration_ms"
        private const val TAG = "CountdownTimerService"
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
        createNotificationChannel()
        listenToTimer()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID_TIMER_SERVICE,
            CHANNEL_NAME_TIMER_SERVICE,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun listenToTimer() {
        scope.launch {
            var lastTick: Long? = null
            CountdownTimer.globalTimerFlow()
                .onEach { remainingSeconds ->
                    lastTick = remainingSeconds
                    val remainingMs = remainingSeconds * 1000L
                    // Here is the logging you requested
                    Log.d(TAG, "Remaining time: ${formatMsToMS(remainingMs)}")
                    updateNotification(formatMsToMS(remainingMs))
                }
                .onCompletion {
                    // Only treat as finished if the flow reached 0 naturally
                    val finishedNaturally = (lastTick == 0L)
                    Log.d(TAG, "Timer completed. finishedNaturally=${'$'}finishedNaturally")
                    if (finishedNaturally) {
                        onTimerFinished()
                    }
                }
                .catch { e ->
                    Log.e(TAG, "Error in timer flow", e)
                }
                .collect {} // Start collecting the flow
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.name -> {
                val duration = intent.getLongExtra(EXTRA_DURATION_MS, -1L)
                if (duration > 0L) {
                    start(duration)
                }
            }

            Actions.PAUSE.name -> pause() // Note: CountdownTimer just stops.
            Actions.STOP.name -> stop()
        }
        return START_STICKY
    }

    private fun buildNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID_TIMER_SERVICE)
            .setSmallIcon(R.drawable.notification_ic)
            .setContentTitle(getString(R.string.countdown_timer_notification_title))
            .setContentText(contentText)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    private fun start(requestedDurationMs: Long) {
        if (CountdownTimer.isRunning.value) return

        // Acquire wakelock
        if (!wakeLock.isHeld) {
            wakeLock.acquire(requestedDurationMs)
        }

        // Start foreground with initial notification
        startForeground(1, buildNotification(formatMsToMS(requestedDurationMs)))

        // Start the global timer
        CountdownTimer.startGlobal(requestedDurationMs)
    }

    private fun pause() {
        // The global timer does not support pause, so we stop it.
        // A resume would start a new timer.
        CountdownTimer.stop()
        if (wakeLock.isHeld) wakeLock.release()
        updateNotification("Paused")
    }

    private fun stop() {
        CountdownTimer.stop()
        stopSelf()
    }

    private fun onTimerFinished() {
        if (wakeLock.isHeld) wakeLock.release()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_TIMER_SERVICE)
            .setSmallIcon(R.drawable.notification_ic)
            .setContentTitle("Timer finished")
            .setContentText("00:00")
            .setAutoCancel(true)
            .build()
        notificationManager.notify(
            2,
            notification
        ) // Use a different ID to show a separate completion notification
        
        stopSelf()
    }

    private fun updateNotification(contentText: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(1, buildNotification(contentText))
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
