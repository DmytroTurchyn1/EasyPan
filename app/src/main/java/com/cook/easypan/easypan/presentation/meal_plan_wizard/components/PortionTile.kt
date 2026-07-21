package com.cook.easypan.easypan.presentation.meal_plan_wizard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cook.easypan.R
import com.cook.easypan.ui.theme.EasyPanTheme

/**
 * A single-select number tile for the portions step. Selected fills with the primary
 * colour; unselected uses the muted surface variant.
 */
@Composable
fun PortionTile(
    number: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val description = stringResource(R.string.meal_plan_wizard_portions_cd, number)

    Box(
        modifier = modifier
            .height(77.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            color = textColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
private fun PortionTilePreview() {
    EasyPanTheme {
        PortionTile(
            number = 2,
            selected = true,
            onClick = {},
            modifier = Modifier.width(109.dp)
        )
    }
}

@Preview
@Composable
private fun PortionTileUnselectedPreview() {
    EasyPanTheme {
        PortionTile(
            number = 3,
            selected = false,
            onClick = {},
            modifier = Modifier.width(109.dp)
        )
    }
}
