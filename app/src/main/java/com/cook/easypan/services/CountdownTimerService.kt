/*
 * Created  21/8/2025
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
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.cook.easypan.R
import com.cook.easypan.core.util.CHANNEL_ID_TIMER_SERVICE
import com.cook.easypan.core.util.CHANNEL_NAME_TIMER_SERVICE
import com.cook.easypan.core.util.formatMsToMS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CountdownTimerService : Service() {

    companion object {
        const val EXTRA_DURATION_MS = "extra_duration_ms" // pass duration in milliseconds
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timerJob: Job? = null

    // countdown state
    private var totalDurationMs: Long = 0L      // initial duration (optional)
    private var remainingMs: Long = 0L          // remaining ms to count down
    private var endTime: Long = 0L      // SystemClock.elapsedRealtime() + remainingMs
    private var running = false

    // wakelock
    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "${packageName}:CountdownTimer"
        )
        val channel = NotificationChannel(
            CHANNEL_ID_TIMER_SERVICE,
            CHANNEL_NAME_TIMER_SERVICE,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.name -> {
                val duration = intent.getLongExtra(EXTRA_DURATION_MS, -1L)
                if (duration >= 0L) {
                    totalDurationMs = duration
                    remainingMs = duration
                }

                start(duration)
            }

            Actions.PAUSE.name -> pause()
            Actions.STOP.name -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
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

    private fun start(requestedDurationMs: Long = -1L) {
        if (running) return

        if (remainingMs <= 0L) return


        // compute absolute end time in elapsedRealtime domain
        endTime = SystemClock.elapsedRealtime() + remainingMs
        running = true

        // acquire wakelock (short timeout to avoid leaks). adjust timeout as needed.
        if (!wakeLock.isHeld) wakeLock.acquire(requestedDurationMs)

        // start foreground with initial notification
        startForeground(1, buildNotification(formatMsToMS(remainingMs)))

        // update loop
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && running) {
                val restTime = endTime - SystemClock.elapsedRealtime()
                if (restTime <= 0L) {
                    // finished
                    updateNotification(getString(R.string.countdown_timer_notification_finished))
                    onTimerFinished()
                    break
                } else {
                    updateNotification(formatMsToMS(restTime))
                }
                delay(1000L)
            }
        }
    }

    private fun pause() {
        if (!running) return
        // compute remaining and stop loop
        remainingMs = (endTime - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        running = false
        timerJob?.cancel()
        if (wakeLock.isHeld) wakeLock.release()

        // show paused notification
        updateNotification("Paused: ${formatMsToMS(remainingMs)}")
    }

    private fun stop() {
        running = false
        timerJob?.cancel()
        remainingMs = 0L
        totalDurationMs = 0L
        if (wakeLock.isHeld) wakeLock.release()
        stopSelf()
    }

    private fun onTimerFinished() {
        // release wakelock and stop service
        if (wakeLock.isHeld) wakeLock.release()

        // optional: show a final notification or trigger other behavior
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_TIMER_SERVICE)
            .setSmallIcon(R.drawable.notification_ic)
            .setContentTitle("Timer finished")
            .setContentText("00:00")
            .setAutoCancel(true)
            .build()
        notificationManager.notify(1 + 1, notification)

        stopSelf()
    }

    private fun updateNotification(contentText: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(1, buildNotification(contentText))
    }


    override fun onDestroy() {
        timerJob?.cancel()
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