package com.cook.easypan.easypan.presentation.onboarding.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cook.easypan.R
import com.cook.easypan.core.presentation.rememberAnimationsEnabled
import com.cook.easypan.easypan.presentation.onboarding.NOTIFICATION_STACK_FADE_IN_MILLIS
import com.cook.easypan.easypan.presentation.onboarding.staggerRunMillis
import com.cook.easypan.easypan.presentation.onboarding.staggeredFraction
import com.cook.easypan.ui.theme.EasyPanTheme

private val STACK_HEIGHT = 172.dp
private val CARD_CORNER = 16.dp
private val TILE_CORNER = 12.dp
private const val TILE_RATIO = 0.625f
private const val PADDING_RATIO = 0.18f
private const val ICON_RATIO = 0.74f
private const val ICON_ASPECT = 21f / 33f

private data class PreviewCard(
    @param:StringRes val message: Int,
    val widthFraction: Float,
    val height: Dp,
    val topOffset: Dp,
)

private val PREVIEW_CARDS = listOf(
    PreviewCard(R.string.onboarding_notifications_preview_3, 0.824f, 59.dp, 113.dp),
    PreviewCard(R.string.onboarding_notifications_preview_2, 0.920f, 66.dp, 59.dp),
    PreviewCard(R.string.onboarding_notifications_preview_1, 1f, 72.dp, 0.dp),
)

@Composable
fun NotificationPreviewStack(
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.onboarding_notifications_preview_description)
    val elapsed = rememberOneShotElapsed(
        durationMillis = staggerRunMillis(PREVIEW_CARDS.size, NOTIFICATION_STACK_FADE_IN_MILLIS),
        isActive = isActive,
        enabled = rememberAnimationsEnabled()
    )
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(STACK_HEIGHT)
            .clearAndSetSemantics { contentDescription = description }
    ) {
        val availableWidth = maxWidth
        PREVIEW_CARDS.forEachIndexed { index, card ->
            NotificationPreviewCard(
                message = stringResource(card.message),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = card.topOffset)
                    .width(availableWidth * card.widthFraction)
                    .height(card.height)
                    // Reading the fraction inside graphicsLayer defers it to the draw phase.
                    .graphicsLayer {
                        alpha = LinearOutSlowInEasing.transform(
                            staggeredFraction(
                                elapsedMillis = elapsed.value.toInt(),
                                index = index,
                                fadeInMillis = NOTIFICATION_STACK_FADE_IN_MILLIS
                            )
                        )
                    }
            )
        }
    }
}

@Composable
private fun NotificationPreviewCard(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(CARD_CORNER)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                shape = RoundedCornerShape(CARD_CORNER)
            )
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val cardHeight = maxHeight
            val contentPadding = cardHeight * PADDING_RATIO
            val tileSize = cardHeight * TILE_RATIO
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(tileSize)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(TILE_CORNER)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val iconWidth = tileSize * ICON_RATIO
                    Icon(
                        painter = painterResource(R.drawable.ic_frying_pan),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(
                            width = iconWidth,
                            height = iconWidth * ICON_ASPECT
                        )
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.onboarding_notifications_preview_time),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    modifier = Modifier.align(Alignment.Top)
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun NotificationPreviewStackPreview() {
    EasyPanTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            NotificationPreviewStack(
                isActive = true,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}
