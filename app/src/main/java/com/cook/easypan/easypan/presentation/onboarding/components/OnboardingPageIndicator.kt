package com.cook.easypan.easypan.presentation.onboarding.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.cook.easypan.R
import com.cook.easypan.ui.theme.EasyPanTheme
import kotlin.math.abs

private val DOT_SIZE = 8.dp
private val ACTIVE_DOT_WIDTH = 28.dp
private val DOT_SPACING = 8.dp

@Composable
fun OnboardingPageIndicator(
    pageCount: Int,
    currentPage: Int,
    position: () -> Float,
    modifier: Modifier = Modifier,
) {
    val stepLabel = stringResource(R.string.onboarding_page_indicator, currentPage + 1, pageCount)
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant

    // Each dot's weight is 1 - |position - index|, and the two dots either side of the pager
    // position always sum to 1, so the row keeps a constant total width as the pill slides.
    val totalWidth = ACTIVE_DOT_WIDTH +
            DOT_SIZE * (pageCount - 1) +
            DOT_SPACING * (pageCount - 1)

    Canvas(
        modifier = modifier
            .width(totalWidth)
            .height(DOT_SIZE)
            .clearAndSetSemantics { contentDescription = stepLabel }
    ) {
        val dotPx = DOT_SIZE.toPx()
        val activePx = ACTIVE_DOT_WIDTH.toPx()
        val spacingPx = DOT_SPACING.toPx()
        val radius = CornerRadius(dotPx / 2f)
        val current = position()
        var x = 0f
        repeat(pageCount) { index ->
            val weight = (1f - abs(current - index)).coerceIn(0f, 1f)
            val width = lerp(dotPx, activePx, weight)
            drawRoundRect(
                color = lerp(inactiveColor, activeColor, weight),
                topLeft = Offset(x, 0f),
                size = Size(width, dotPx),
                cornerRadius = radius
            )
            x += width + spacingPx
        }
    }
}

@PreviewLightDark
@Composable
private fun OnboardingPageIndicatorPreview() {
    EasyPanTheme {
        OnboardingPageIndicator(pageCount = 5, currentPage = 2, position = { 2f })
    }
}

@PreviewLightDark
@Composable
private fun OnboardingPageIndicatorMidSwipePreview() {
    EasyPanTheme {
        OnboardingPageIndicator(pageCount = 5, currentPage = 2, position = { 2.5f })
    }
}
