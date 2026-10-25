package com.cook.easypan.easypan.presentation.onboarding.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

private val CARD_CORNER = 16.dp
private val CARD_BORDER = 1.dp

internal val TRACK_HEIGHT = 10.dp

internal fun Modifier.onboardingCard(
    containerColor: Color,
    borderColor: Color,
    corner: Dp = CARD_CORNER,
): Modifier = this
    .background(color = containerColor, shape = RoundedCornerShape(corner))
    .border(width = CARD_BORDER, color = borderColor, shape = RoundedCornerShape(corner))

@Composable
internal fun OnboardingProgressTrack(
    progress: () -> Float,
    trackColor: Color,
    fillColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(TRACK_HEIGHT)
    ) {
        // Reading progress() here, inside the draw lambda, keeps the sweep in the draw phase so it
        // never invalidates composition.
        val radius = CornerRadius(size.height / 2f)
        drawRoundRect(color = trackColor, cornerRadius = radius)
        val fillWidth = size.width * progress().coerceIn(0f, 1f)
        if (fillWidth > 0f) {
            drawRoundRect(
                color = fillColor,
                size = Size(max(fillWidth, size.height), size.height),
                cornerRadius = radius
            )
        }
    }
}
