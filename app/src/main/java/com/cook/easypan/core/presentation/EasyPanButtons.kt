package com.cook.easypan.core.presentation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun EasyPanButtonPrimary(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Button(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContentColor = MaterialTheme.colorScheme.surfaceBright,
            disabledContainerColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.5f)
        ),
        onClick = {
            onClick()
        },
        enabled = enabled
    ) {
        content()
    }

}

@Composable
fun EasyPanButtonSecondary(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Button(
        modifier = modifier,
        colors = ButtonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            disabledContentColor = MaterialTheme.colorScheme.surfaceBright,
            disabledContainerColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.5f)
        ),
        onClick = {
            onClick()
        },
        enabled = enabled
    ) {
        content()
    }

}

@Preview
@Composable
private fun EasyPanButtonPrimaryPreview() {
    EasyPanButtonPrimary(
        onClick = { /* Do nothing */ },
        content = {
            Text(text = "Primary Button")
        }
    )

}

@Preview
@Composable
private fun EasyPanButtonSecondaryPreview() {
    EasyPanButtonSecondary(
        onClick = { /* Do nothing */ },
        content = {
            Text(text = "Secondary Button")
        }
    )

}