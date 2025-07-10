package com.cook.easypan.easypan.presentation.recipe_detail.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp

@Composable
fun RecipeChip(
    modifier: Modifier = Modifier,
    text: String,
) {
    Box(
        modifier = modifier
            .widthIn(
                min = 50.dp
            )
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.onSurface)
            .padding(
                vertical = 8.dp,
                horizontal = 12.dp
            ),
        contentAlignment = Alignment.Center
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            Text(
                text = text,
            )
        }
    }
}