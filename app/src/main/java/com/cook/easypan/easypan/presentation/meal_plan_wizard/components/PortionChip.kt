package com.cook.easypan.easypan.presentation.meal_plan_wizard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun PortionChip(
    modifier: Modifier = Modifier,
    portion: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .padding(
                    vertical = 28.dp,
                    horizontal = 49.dp
                ),
            text = "$portion",
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize
        )

    }
}


@Preview
@Composable
private fun PortionChipSelectedPreview() {
    EasyPanTheme {
        PortionChip(
            portion = 2,
            selected = true,
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun PortionChipUnselectedPreview() {
    EasyPanTheme {
        PortionChip(
            portion = 2,
            selected = false,
            onClick = {}
        )
    }
}