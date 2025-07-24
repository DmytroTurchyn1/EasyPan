package com.cook.easypan.easypan.presentation.recipe_step.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cook.easypan.R
import com.cook.easypan.ui.theme.EasyPanTheme
import kotlinx.coroutines.delay

@Composable
fun TimerStepRecipe(
    totalSeconds: Int?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        var timeSecondsLeft by remember { mutableIntStateOf(totalSeconds?.rem(60) ?: 0) }
        var timeMinutesLeft by remember { mutableIntStateOf(totalSeconds?.div(60) ?: 0) }
        var isPaused by remember { mutableStateOf(true) }

        LaunchedEffect(timeSecondsLeft, timeMinutesLeft, isPaused) {
            while ((timeMinutesLeft > 0 || timeSecondsLeft > 0) && !isPaused) {
                delay(1000L)
                if (timeSecondsLeft > 0) {
                    timeSecondsLeft--
                } else if (timeMinutesLeft > 0) {
                    timeMinutesLeft--
                    timeSecondsLeft = 59
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                TimerCounterElement(
                    title = stringResource(R.string.minutes_timer),
                    time = timeMinutesLeft
                )
                TimerCounterElement(
                    title = stringResource(R.string.seconds_timer),
                    time = timeSecondsLeft
                )
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    isPaused = false
                },
                enabled = isPaused,
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    disabledContentColor = MaterialTheme.colorScheme.surfaceBright,
                    disabledContainerColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = stringResource(R.string.start_button_timer),
                )
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    isPaused = true
                },
                enabled = !isPaused,
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    disabledContentColor = MaterialTheme.colorScheme.surfaceBright,
                    disabledContainerColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = stringResource(R.string.stop_button_timer),
                )
            }
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