/*
 * Created  27/7/2026
 *
 * Copyright (c) 2026 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.paywall

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cook.easypan.R
import com.cook.easypan.core.presentation.EasyPanButtonPrimary
import com.cook.easypan.core.presentation.snackBar.SnackBarController
import com.cook.easypan.core.presentation.snackBar.SnackBarEvent
import com.cook.easypan.core.presentation.toMessageRes
import com.cook.easypan.ui.theme.EasyPanTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun PaywallRoot(
    viewModel: PaywallViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PaywallScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
private fun PaywallScreen(
    state: PaywallState,
    onAction: (PaywallAction) -> Unit,
) {
    val context = LocalContext.current

    state.error?.let { error ->
        val errorMessage = stringResource(error.toMessageRes())
        LaunchedEffect(error) {
            SnackBarController.sendEvent(SnackBarEvent(message = errorMessage))
        }
    }
    if (state.isRestored) {
        val restoredMessage = stringResource(R.string.paywall_restore_success)
        LaunchedEffect(Unit) {
            SnackBarController.sendEvent(SnackBarEvent(message = restoredMessage))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeroImage()
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProChip()
            Spacer(modifier = Modifier.width(11.dp))
            Text(
                text = stringResource(R.string.paywall_brand),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.paywall_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.paywall_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(28.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            FeatureRow(text = stringResource(R.string.paywall_feature_plan))
            FeatureRow(text = stringResource(R.string.paywall_feature_allergies))
            FeatureRow(text = stringResource(R.string.paywall_feature_swap))
        }
        Spacer(modifier = Modifier.height(44.dp))
        EasyPanButtonPrimary(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 52.dp)
                .height(48.dp),
            onClick = { onAction(PaywallAction.OnPurchaseClick(context)) },
            enabled = state.canPurchase
        ) {
            Text(
                text = stringResource(R.string.paywall_cta),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        state.priceFormatted?.let { price ->
            Text(
                text = stringResource(R.string.paywall_price, price),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
        TextButton(
            onClick = { onAction(PaywallAction.OnRestoreClick) },
            enabled = !state.isPurchasing
        ) {
            Text(
                text = stringResource(R.string.paywall_restore),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun HeroImage(
    modifier: Modifier = Modifier,
) {
    val background = MaterialTheme.colorScheme.background
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(327.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.paywall_hero),
            contentDescription = stringResource(R.string.paywall_hero_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Bottom 69dp fades into the page background; measured off the Figma overlay asset:
        // transparent until ~35% of the band, fully opaque background by ~80%.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(69.dp)
                .background(
                    Brush.verticalGradient(
                        0.35f to Color.Transparent,
                        0.8f to background,
                        1f to background
                    )
                )
        )
    }
}

@Composable
private fun ProChip(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 10.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.paywall_chip),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FeatureRow(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaywallScreenPreview() {
    EasyPanTheme {
        PaywallScreen(
            state = PaywallState(isLoading = false, priceFormatted = "$4.99"),
            onAction = {}
        )
    }
}
