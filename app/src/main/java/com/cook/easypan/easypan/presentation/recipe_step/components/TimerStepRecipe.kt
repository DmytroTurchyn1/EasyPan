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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun TimerStepRecipe(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                TimerCounterElement(
                    title = "Minutes"
                )
                TimerCounterElement(
                    title = "Seconds"
                )
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                onClick = {}
            ) {
                Text(
                    text = "Start Timer"
                )
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {},
                enabled = false
            ) {
                Text(
                    text = "Stop Timer"
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

        )
    }
}