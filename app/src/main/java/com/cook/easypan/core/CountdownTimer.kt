/*
 * Created  8/9/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.core

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object CountdownTimer {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    // null = idle; 0 = finished (until stop() resets to idle).
    private val _remainingSeconds = MutableStateFlow<Long?>(null)
    val remainingSeconds: StateFlow<Long?> = _remainingSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    // Who started the timer (the recipe step index). Lets the UI tell this
    // step's countdown apart from another step's still-running timer.
    private val _ownerId = MutableStateFlow<Int?>(null)
    val ownerId: StateFlow<Int?> = _ownerId.asStateFlow()

    fun startGlobal(durationMs: Long, ownerId: Int? = null) {
        job?.cancel()
        _ownerId.value = ownerId
        if (durationMs <= 0L) {
            _remainingSeconds.value = 0L
            _isRunning.value = false
            job = null
            return
        }
        _isRunning.value = true
        val newJob = scope.launch {
            val end = SystemClock.elapsedRealtime() + durationMs
            while (isActive) {
                val now = SystemClock.elapsedRealtime()
                val remainingMs = (end - now).coerceAtLeast(0L)
                _remainingSeconds.value = remainingMs / 1000L
                if (remainingMs <= 0L) break
                val nextTickRemainder = remainingMs % 1000L
                val delayMs = if (nextTickRemainder == 0L) 1000L else nextTickRemainder
                delay(delayMs)
            }
        }
        newJob.invokeOnCompletion { cause ->
            if (cause == null) {
                // Natural completion: remaining stays at 0 so observers see it;
                // stop() resets the timer to idle.
                _isRunning.value = false
            }
        }
        job = newJob
    }

    // Stops ticking but keeps the remaining time and owner, so the timer can
    // be resumed by starting it again with the remaining duration.
    fun pause() {
        job?.cancel()
        job = null
        _isRunning.value = false
    }

    fun stop() {
        job?.cancel()
        job = null
        _isRunning.value = false
        _remainingSeconds.value = null
        _ownerId.value = null
    }
}
