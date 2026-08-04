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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cook.easypan.R
import com.cook.easypan.core.presentation.EasyPanButtonSecondary
import com.cook.easypan.core.presentation.EasyPanText
import com.cook.easypan.easypan.presentation.recipe_step.RecipeStepAction
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun TimerStepRecipe(
    totalSeconds: Int?,
    stepIndex: Int,
    remainingSeconds: Long?,
    ownerStep: Int?,
    isRunning: Boolean,
    onAction: (RecipeStepAction) -> Unit
) {
    if (totalSeconds != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // The global timer may belong to another step; only the owning
            // step shows its countdown, every other step shows its own total.
            val isMine = ownerStep == stepIndex
            val displaySeconds = (if (isMine) remainingSeconds else null) ?: totalSeconds.toLong()

            val mins = (displaySeconds / 60L).toInt()
            val secs = (displaySeconds % 60L).toInt()

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
                        onAction(RecipeStepAction.OnTimerToggleClick(stepIndex, totalSeconds))
                    },
                    enabled = true
                ) {
                    Text(
                        text = if (isRunning && isMine) stringResource(R.string.stop_button_timer) else stringResource(
                            R.string.start_button_timer
                        ),
                    )
                }
                EasyPanButtonSecondary(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        onAction(RecipeStepAction.OnTimerRestartClick(stepIndex))
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
            totalSeconds = 1200,
            stepIndex = 0,
            remainingSeconds = 754,
            ownerStep = 0,
            isRunning = true,
            onAction = {}
        )
    }
}
