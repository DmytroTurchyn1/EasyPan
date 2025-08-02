package com.cook.easypan.easypan.presentation.recipe_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun IngredientsItem(
    checked: Boolean = false,
    onCheckClick: () -> Unit,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheckClick() },
        )
        Text(
            text = text,
            modifier = Modifier
                .weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize
        )
    }
}

@Preview
@Composable
private fun IngredientsItemPreview() {
    EasyPanTheme {
        IngredientsItem(
            text = "1 cup of flour",
            onCheckClick = {}
        )
    }
}