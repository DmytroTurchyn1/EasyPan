/*
 * Created  14/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RecipeFilterBar(
    modifier: Modifier = Modifier,
    onFilterSelected: (String) -> Unit,
    selected: String,
    filters: List<String>
) {

    LazyRow(
        modifier = modifier
            .padding(start = 4.dp, end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = filters,
            key = { it }
        ) { chip ->

            RecipeFilterChip(
                filter = chip,
                onClick = {

                    onFilterSelected(chip)
                },
                selected = chip == selected
            )
        }
    }
}