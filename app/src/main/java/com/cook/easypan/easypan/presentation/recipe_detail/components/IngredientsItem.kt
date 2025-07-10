package com.cook.easypan.easypan.presentation.recipe_detail.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ingredientsItem(
    modifier: Modifier = Modifier,
    onCheckClick : () -> Unit,
    text: String
) {
    Row (
        modifier = Modifier.fillMaxWidth()
    ){
        Checkbox(
            checked = false,
            onCheckedChange = {onCheckClick},
        )
        Text(
            text = text,
            modifier = Modifier
                .weight(1f),
            color = Color.White
        )
    }
}