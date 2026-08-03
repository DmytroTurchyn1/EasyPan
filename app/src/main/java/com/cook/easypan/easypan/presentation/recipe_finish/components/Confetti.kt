package com.cook.easypan.easypan.presentation.recipe_finish.components

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cook.easypan.ui.theme.EasyPanTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

private const val DEFAULT_PARTICLE_COUNT = 80

// Long enough that the slowest piece has left the screen (measured worst case ~4.3s) before the
// tail fade starts, so the fade only ever acts as a safety net and is never seen.
private const val DEFAULT_DURATION_MILLIS = 4800
private const val FADE_OUT_SECONDS = 0.4f
private const val MIN_SQUASH = 0.12f
private const val DISC_CHANCE = 0.25f

private val ConfettiColors = listOf(
    Color(0xFFFFC107), // gold
    Color(0xFFFF6F61), // coral
    Color(0xFFEC407A), // pink
    Color(0xFF7E57C2), // violet
    Color(0xFF26C6DA), // teal
)

/**
 * A one-shot celebration overlay. Pieces are emitted above the top edge over the first second, then
 * flutter down under aerodynamic drag — each at its own terminal velocity, so they spread apart as
 * they fall instead of descending as a clump.
 *
 * Draws on a [Canvas], which does not consume touch events, so anything underneath stays tappable.
 */
@Composable
fun Confetti(
    modifier: Modifier = Modifier,
    particleCount: Int = DEFAULT_PARTICLE_COUNT,
    durationMillis: Int = DEFAULT_DURATION_MILLIS,
    onFinished: () -> Unit = {},
) {
    val context = LocalContext.current
    val animationsDisabled = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }

    if (animationsDisabled) {
        LaunchedEffect(Unit) { onFinished() }
        return
    }

    val particles = remember(particleCount) { createParticles(particleCount) }
    val elapsedSeconds = remember { Animatable(0f) }
    val durationSeconds = durationMillis / 1000f

    LaunchedEffect(Unit) {
        elapsedSeconds.animateTo(
            targetValue = durationSeconds,
            animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing)
        )
        onFinished()
    }

    Canvas(modifier = modifier.clearAndSetSemantics { }) {
        // Reading the animated value here, inside the draw lambda, keeps the whole animation in the
        // draw phase — it never triggers a recomposition.
        drawConfetti(
            particles = particles,
            elapsed = elapsedSeconds.value,
            durationSeconds = durationSeconds
        )
    }
}

/**
 * A single piece of paper. Distances are fractions of the canvas rather than pixels so a piece takes
 * the same time to cross any screen; only [widthDp] is a physical size.
 */
private data class Confetto(
    val delaySeconds: Float,
    val startXFraction: Float,
    val startYFraction: Float,
    val fallSpeed: Float,
    val dragRate: Float,
    val drift: Float,
    val swayAmplitude: Float,
    val swaySpeed: Float,
    val swayPhase: Float,
    val spinSpeed: Float,
    val widthDp: Float,
    val aspect: Float,
    val alphaScale: Float,
    val isDisc: Boolean,
    val color: Color,
)

private fun createParticles(count: Int): List<Confetto> = List(count) {
    Confetto(
        delaySeconds = Random.nextFloat(),
        startXFraction = Random.nextFloat(),
        startYFraction = -0.03f - Random.nextFloat() * 0.15f,
        fallSpeed = 0.38f + Random.nextFloat() * 0.40f,
        dragRate = 3.5f + Random.nextFloat() * 2.5f,
        drift = Random.nextFloat() * 0.1f - 0.05f,
        swayAmplitude = 0.008f + Random.nextFloat() * 0.027f,
        swaySpeed = 1.5f + Random.nextFloat() * 3f,
        swayPhase = Random.nextFloat() * 2f * PI.toFloat(),
        spinSpeed = Random.nextFloat() * 440f - 220f,
        widthDp = 5f + Random.nextFloat() * 7f,
        aspect = 1.2f + Random.nextFloat() * 1.6f,
        alphaScale = 0.85f + Random.nextFloat() * 0.15f,
        isDisc = Random.nextFloat() < DISC_CHANCE,
        color = ConfettiColors.random(),
    )
}

private fun DrawScope.drawConfetti(
    particles: List<Confetto>,
    elapsed: Float,
    durationSeconds: Float,
) {
    val fadeStart = durationSeconds - FADE_OUT_SECONDS
    val tailAlpha = if (elapsed <= fadeStart) {
        1f
    } else {
        ((durationSeconds - elapsed) / FADE_OUT_SECONDS).coerceIn(0f, 1f)
    }
    if (tailAlpha == 0f) return

    particles.forEach { particle ->
        val t = elapsed - particle.delaySeconds
        if (t <= 0f) return@forEach

        // Linear drag from rest: the piece reaches its own terminal velocity within roughly half a
        // second and holds it. Because every piece has a different terminal velocity, the gaps
        // between them keep growing — which is what stops the group moving as one block.
        val fallDistance =
            particle.fallSpeed * (t - (1f - exp(-particle.dragRate * t)) / particle.dragRate)
        val y = (particle.startYFraction + fallDistance) * size.height

        val width = particle.widthDp.dp.toPx()
        if (y > size.height + width * particle.aspect) return@forEach

        // One phase drives both the sideways wobble and the edge-on flip, so a piece looks like it
        // is turning into and out of the air it is falling through.
        val phase = particle.swaySpeed * t + particle.swayPhase
        val x = (particle.startXFraction + particle.drift * t) * size.width +
                particle.swayAmplitude * sin(phase) * size.width

        val squash = abs(cos(phase)).coerceAtLeast(MIN_SQUASH)
        val height = width * particle.aspect * squash
        val alpha = tailAlpha * particle.alphaScale
        val topLeft = Offset(x - width / 2f, y - height / 2f)
        val particleSize = Size(width, height)

        rotate(degrees = particle.spinSpeed * t, pivot = Offset(x, y)) {
            if (particle.isDisc) {
                drawOval(
                    color = particle.color,
                    topLeft = topLeft,
                    size = particleSize,
                    alpha = alpha
                )
            } else {
                drawRect(
                    color = particle.color,
                    topLeft = topLeft,
                    size = particleSize,
                    alpha = alpha
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfettiPreview() {
    EasyPanTheme {
        // Animations do not run in a static preview, so draw fixed frames instead: one early and one
        // mid-fall, where the pieces have spread out.
        val particles = remember { createParticles(DEFAULT_PARTICLE_COUNT) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1f, 2.5f).forEach { elapsed ->
                Canvas(modifier = Modifier.size(200.dp, 420.dp)) {
                    drawConfetti(particles = particles, elapsed = elapsed, durationSeconds = 5f)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfettiFullScreenPreview() {
    EasyPanTheme {
        Confetti(modifier = Modifier.fillMaxSize())
    }
}
