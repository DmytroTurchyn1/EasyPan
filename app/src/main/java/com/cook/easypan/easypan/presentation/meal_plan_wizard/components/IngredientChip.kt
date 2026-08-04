package com.cook.easypan.easypan.presentation.meal_plan_wizard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun IngredientChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .widthIn(min = 40.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(
                vertical = 8.dp,
                horizontal = 8.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize
            )
        }
    }
}

@Preview
@Composable
private fun IngredientChipSelectedPreview() {
    EasyPanTheme {
        IngredientChip(
            text = "Quinoa",
            selected = true,
            onClick = {},
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview
@Composable
private fun IngredientChipSuggestionPreview() {
    EasyPanTheme {
        IngredientChip(
            text = "Smoothie",
            selected = false,
            onClick = {},
            modifier = Modifier.padding(8.dp)
        )
    }
}
