package com.cook.easypan.easypan.presentation.recipe_step.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
        var timeSecondsLeft by remember { mutableIntStateOf(totalSeconds?.rem(60) ?: 60) }
        var timeMinutesLeft by remember { mutableIntStateOf(totalSeconds?.div(60) ?: 60) }
        var isPaused by remember { mutableStateOf(true) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {

                LaunchedEffect(timeSecondsLeft, isPaused) {
                    while (timeMinutesLeft > 0 && !isPaused) {
                        delay(1000L)
                        if (timeSecondsLeft == 0) {
                            timeMinutesLeft--
                            timeSecondsLeft = 60
                        }
                        timeSecondsLeft--
                    }
                }
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
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                onClick = {
                    isPaused = false
                },
                enabled = isPaused
            ) {
                Text(
                    text = stringResource(R.string.start_button_timer)
                )
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    isPaused = true
                },
                enabled = !isPaused
            ) {
                Text(
                    text = stringResource(R.string.stop_button_timer)
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