package com.cook.easypan.easypan.presentation.recipe_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cook.easypan.R
import com.cook.easypan.ui.theme.EasyPanTheme


@Composable
fun SelectedIngredientsChip(
    modifier: Modifier = Modifier,
    selected: Int,
    total: Int
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = stringResource(R.string.ingredients_count, selected, total),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp),

            )
    }

}

@Preview
@Composable
private fun SelectedIngredientsChipPreview() {
    EasyPanTheme {
        SelectedIngredientsChip(
            selected = 1,
            total = 5
        )
    }
}