package com.cook.easypan.easypan.presentation.recipe_detail.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.cook.easypan.ui.theme.EasyPanTheme

/**
 * Displays a horizontal row with a title and corresponding text value.
 *
 * The title occupies proportional space and uses the theme's onSurface color, while the text value is displayed in white.
 *
 * @param title The label or heading to display on the left.
 * @param text The value or detail to display on the right.
 */
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
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = text,
            modifier = Modifier,
            color = Color.White
        )
    }
}

/**
 * Displays a preview of the RecipeItem composable with sample data and a semi-transparent background.
 */
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