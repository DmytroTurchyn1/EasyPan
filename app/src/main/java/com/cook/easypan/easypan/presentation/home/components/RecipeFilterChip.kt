/*
 * Created  15/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun RecipeFilterChip(
    filter: String,
    onClick: () -> Unit,
    selected: Boolean = false
) {
    Box(
        modifier = Modifier
            .clip(
                RoundedCornerShape(16.dp)
            )
            .selectable(selected = selected, onClick = onClick)
            .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = filter,
            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(
                    min = 40.dp
                )

                .padding(
                    vertical = 8.dp,
                    horizontal = 8.dp
                )
        )
    }
}

@Preview
@Composable
private fun RecipeFilterChipPreview() {
    EasyPanTheme {
        RecipeFilterChip(
            filter = "Vegetarian",
            onClick = {},
        )
    }
}