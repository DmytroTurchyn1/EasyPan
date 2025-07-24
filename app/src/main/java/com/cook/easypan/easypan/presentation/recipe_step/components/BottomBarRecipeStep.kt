package com.cook.easypan.easypan.presentation.recipe_step.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cook.easypan.R
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun BottomBarRecipeStep(
    modifier: Modifier = Modifier,
    enabledPrevious: Boolean = false,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    nextButtonTitle: String
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onPreviousClick() },
                enabled = enabledPrevious,
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContentColor = MaterialTheme.colorScheme.surfaceBright,
                    disabledContainerColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = stringResource(R.string.previous_button),
                )
            }
            Button(
                onClick = { onNextClick() },
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContentColor = MaterialTheme.colorScheme.surfaceBright,
                    disabledContainerColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = nextButtonTitle,
                )
            }
        }

    }
}

@Preview
@Composable
private fun BottomBarRecipeStepPreview() {
    EasyPanTheme {
        BottomBarRecipeStep(
            onNextClick = {},
            onPreviousClick = {},
            nextButtonTitle = "Next"
        )
    }
}