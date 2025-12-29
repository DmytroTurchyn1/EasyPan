/*
 * Created  8/9/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.recipe_step.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cook.easypan.R
import com.cook.easypan.core.CountdownTimer
import com.cook.easypan.core.presentation.EasyPanButtonSecondary
import com.cook.easypan.core.presentation.EasyPanText
import com.cook.easypan.core.util.Launcher.pauseTimerService
import com.cook.easypan.core.util.Launcher.startTimerService
import com.cook.easypan.core.util.Launcher.stopTimerService
import com.cook.easypan.ui.theme.EasyPanTheme
import kotlinx.coroutines.launch

@Composable
fun TimerStepRecipe(
    totalSeconds: Int?
) {
    if (totalSeconds != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            val running by CountdownTimer.isRunning.collectAsState(initial = false)
            val remainingOrNull by CountdownTimer.remainingSeconds.collectAsState(initial = null)

            var lastSeconds by remember { mutableLongStateOf(totalSeconds.toLong()) }
            LaunchedEffect(remainingOrNull) {
                remainingOrNull?.let { lastSeconds = it }
            }

            val mins = (lastSeconds / 60L).toInt()
            val secs = (lastSeconds % 60L).toInt()

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                EasyPanText(
                    text = stringResource(R.string.countdown_timer_title),
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    TimerCounterElement(
                        title = stringResource(R.string.minutes_timer),
                        time = mins
                    )
                    TimerCounterElement(
                        title = stringResource(R.string.seconds_timer),
                        time = secs
                    )
                }
                EasyPanButtonSecondary(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            if (!running) {

                                val startFrom =
                                    if (lastSeconds in 1 until totalSeconds.toLong()) lastSeconds else totalSeconds.toLong()
                                startTimerService(
                                    context,
                                    startFrom * 1000L,
                                )
                            } else {
                                pauseTimerService(context)
                            }
                        }
                    },
                    enabled = true
                ) {
                    Text(
                        text = if (!running) stringResource(R.string.start_button_timer) else stringResource(
                            R.string.stop_button_timer
                        ),
                    )
                }
                EasyPanButtonSecondary(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        stopTimerService(context)
                        lastSeconds = totalSeconds.toLong()
                    },
                    enabled = true
                ) {
                    Text(
                        text = stringResource(R.string.restart_button_timer),
                    )
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Preview
@Composable
private fun TimerStepRecipePreview() {
    EasyPanTheme {
        TimerStepRecipe(
            totalSeconds = 1200
        )
    }
}