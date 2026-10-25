package com.cook.easypan.easypan.presentation.onboarding.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cook.easypan.R
import kotlin.math.abs

private val HERO_HEIGHT = 352.dp
private val SCRIM_OFFSET = 288.dp
private val SCRIM_HEIGHT = 90.dp

// The image lags behind the page it sits on, which reads as depth. The overscan that hides the
// resulting gap is applied only while the page is off-centre, so the settled page keeps the exact
// crop from the design. It has to grow at more than twice PARALLAX_FACTOR to stay ahead of the lag.
private const val PARALLAX_FACTOR = 0.2f
private const val PARALLAX_OVERSCAN = 0.6f

@Composable
fun OnboardingHero(
    @DrawableRes image: Int,
    pageOffset: () -> Float,
    modifier: Modifier = Modifier,
) {
    val background = MaterialTheme.colorScheme.background
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HERO_HEIGHT)
            .clipToBounds()
    ) {
        Image(
            painter = painterResource(id = image),
            contentDescription = stringResource(R.string.onboarding_image_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val offset = pageOffset()
                    val scale = 1f + abs(offset) * PARALLAX_OVERSCAN
                    translationX = offset * size.width * PARALLAX_FACTOR
                    scaleX = scale
                    scaleY = scale
                }
        )
        Box(
            modifier = Modifier
                .offset(y = SCRIM_OFFSET)
                .fillMaxWidth()
                .height(SCRIM_HEIGHT)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.394f to background,
                        1f to background
                    )
                )
        )
    }
}
