/*
 * Created  27/7/2026
 *
 * Copyright (c) 2026 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.authentication.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cook.easypan.R
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    // surfaceContainerLowest is pure white in light (matching the design) but darker than the
    // background in dark, so the dark theme steps up to a raised container instead.
    val containerColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }
    OutlinedButton(
        modifier = modifier.height(59.dp),
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
            // The default outlined disabled container is transparent, which would make the button
            // vanish while sign-in is in flight. Keep it filled and just dim it instead.
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        )
    ) {
        Image(
            painter = painterResource(R.drawable.google_logo),
            contentDescription = stringResource(R.string.google_icon_description),
            modifier = Modifier.size(25.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.continue_with_google),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview
@Composable
private fun GoogleSignInButtonPreview() {
    EasyPanTheme {
        GoogleSignInButton(onClick = {})
    }
}
