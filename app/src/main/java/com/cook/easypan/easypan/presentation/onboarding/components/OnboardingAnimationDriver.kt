package com.cook.easypan.easypan.presentation.onboarding.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

/**
 * Drives a one-shot onboarding animation: it runs once when the page settles and then holds on its
 * final frame. Callers read the returned elapsed millis inside a draw lambda so the run stays in the
 * draw phase.
 *
 * The rearm happens when the page goes *inactive*, not when it becomes active again. The pager only
 * settles once a page is fully off-screen or fully on-screen, so resetting on the way out is
 * invisible and the page is already back at frame zero when it scrolls in; resetting on the way in
 * would show the finished frame for the whole swipe and then visibly jump back to the start.
 */
@Composable
internal fun rememberOneShotElapsed(
    durationMillis: Int,
    isActive: Boolean,
    enabled: Boolean,
): State<Float> {
    val elapsed = remember { Animatable(0f) }
    LaunchedEffect(isActive, enabled, durationMillis) {
        when {
            !enabled -> elapsed.snapTo(durationMillis.toFloat())
            isActive -> elapsed.animateTo(
                targetValue = durationMillis.toFloat(),
                animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing)
            )

            else -> elapsed.snapTo(0f)
        }
    }
    return elapsed.asState()
}
