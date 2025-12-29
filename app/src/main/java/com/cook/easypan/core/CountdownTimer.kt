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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object CountdownTimer {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    private val _remainingSeconds = MutableStateFlow<Long?>(null)
    val remainingSeconds: StateFlow<Long?> = _remainingSeconds.asStateFlow()


    val isRunning: StateFlow<Boolean> = _remainingSeconds
        .map { it != null && it >= 0L }
        .stateIn(scope, SharingStarted.Eagerly, false)

    fun startGlobal(durationMs: Long) {

        job?.cancel()
        if (durationMs <= 0L) {

            _remainingSeconds.value = 0L
            _remainingSeconds.value = null

            job = null
            return
        }
        val newJob = scope.launch {
            val end = SystemClock.elapsedRealtime() + durationMs
            while (isActive) {
                val now = SystemClock.elapsedRealtime()
                val remainingMs = (end - now).coerceAtLeast(0L)
                val sec = remainingMs / 1000L
                _remainingSeconds.value = sec
                if (remainingMs <= 0L) break
                val nextTickRemainder = remainingMs % 1000L
                val delayMs = if (nextTickRemainder == 0L) 1000L else nextTickRemainder
                delay(delayMs)
            }
        }
        newJob.invokeOnCompletion { cause ->
            if (cause == null) {
                _remainingSeconds.value = 0L
                _remainingSeconds.value = null
            }
        }
        job = newJob

    }

    fun stop() {
        job?.cancel()
        job = null
        _remainingSeconds.value = null
    }

    fun globalTimerFlow(): Flow<Long> = remainingSeconds
        .filterNotNull()
        .distinctUntilChanged()
}