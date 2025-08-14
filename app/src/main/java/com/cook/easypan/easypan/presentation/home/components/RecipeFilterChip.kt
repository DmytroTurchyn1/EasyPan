/*
 * Created  14/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
    modifier: Modifier = Modifier,
    filter: String,
    onClick: () -> Unit,
    selected: Boolean = false
) {
    Box(
        modifier = Modifier
            .clip(
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = filter,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            textAlign = TextAlign.Center,
            modifier = modifier
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
            modifier = Modifier.padding(8.dp)
        )
    }
}