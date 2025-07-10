package com.cook.easypan.easypan.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SettingsItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    clickable: Boolean = true,
    actionElement: @Composable () -> Unit,
) {
    Box (
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable(enabled = clickable, onClick = onClick)
    ){
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .weight(1f),
            )
            actionElement()
        }

    }
}