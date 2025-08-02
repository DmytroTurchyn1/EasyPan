package com.cook.easypan.easypan.presentation.recipe_detail.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun RecipeItem(
    title: String,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = title,
            modifier = Modifier
                .weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize
        )
        Text(
            text = text,
            modifier = Modifier,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize
        )
    }
}

@Preview(showBackground = true, showSystemUi = false, backgroundColor = 0x5CFFFFFF)
@Composable
private fun RecipeItemPreview() {
    EasyPanTheme {
        RecipeItem(
            text = "15 min",
            title = "Prep"
        )
    }
}