package com.cook.easypan.easypan.presentation.onboarding.components

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cook.easypan.R
import com.cook.easypan.core.presentation.EasyPanText
import com.cook.easypan.core.presentation.rememberAnimationsEnabled
import com.cook.easypan.easypan.presentation.onboarding.STEP_CARD_RUN_MILLIS
import com.cook.easypan.easypan.presentation.onboarding.STEP_CARD_STEP_COUNT
import com.cook.easypan.easypan.presentation.onboarding.stepFrameFor
import com.cook.easypan.ui.theme.EasyPanTheme

private val CARD_HEIGHT = 225.dp
private val CARD_TOP_PADDING = 12.dp
private val TRACK_INSET = 29.dp
private val TITLE_SLOT_HEIGHT = 32.dp
private val DESCRIPTION_SLOT_HEIGHT = 64.dp
private const val TEXT_FADE_MILLIS = 260

private data class PreviewStep(
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
)

private val PREVIEW_STEPS = listOf(
    PreviewStep(R.string.onboarding_step_1_title, R.string.onboarding_step_1_description),
    PreviewStep(R.string.onboarding_step_2_title, R.string.onboarding_step_2_description),
    PreviewStep(R.string.onboarding_step_3_title, R.string.onboarding_step_3_description),
    PreviewStep(R.string.onboarding_step_4_title, R.string.onboarding_step_4_description),
    PreviewStep(R.string.onboarding_step_5_title, R.string.onboarding_step_5_description),
    PreviewStep(R.string.onboarding_step_6_title, R.string.onboarding_step_6_description),
)

@Composable
fun StepPreviewCard(
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val elapsed = rememberOneShotElapsed(
        durationMillis = STEP_CARD_RUN_MILLIS,
        isActive = isActive,
        enabled = rememberAnimationsEnabled()
    )
    // Deriving only the index keeps composition invalidating once per step instead of per frame.
    val stepIndex by remember(elapsed) {
        derivedStateOf { stepFrameFor(elapsed.value.toInt()).index }
    }
    StepPreviewCardContent(
        stepIndex = stepIndex,
        progress = { stepFrameFor(elapsed.value.toInt()).progress },
        modifier = modifier
    )
}

@Composable
private fun StepPreviewCardContent(
    stepIndex: Int,
    progress: () -> Float,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.onboarding_step_card_description)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(CARD_HEIGHT)
            .onboardingCard(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            .padding(top = CARD_TOP_PADDING)
            .clearAndSetSemantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(
            targetState = stepIndex,
            transitionSpec = { textFadeSpec() },
            label = "stepPreviewCounter"
        ) { index ->
            EasyPanText(
                text = stringResource(
                    R.string.onboarding_step_card_counter,
                    index + 1,
                    STEP_CARD_STEP_COUNT
                ),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OnboardingProgressTrack(
            progress = progress,
            trackColor = MaterialTheme.colorScheme.secondaryContainer,
            fillColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = TRACK_INSET)
        )

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedContent(
            targetState = stepIndex,
            transitionSpec = { textFadeSpec() },
            label = "stepPreviewText"
        ) { index ->
            val step = PREVIEW_STEPS[index]
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.height(TITLE_SLOT_HEIGHT),
                    contentAlignment = Alignment.Center
                ) {
                    EasyPanText(
                        text = stringResource(step.title),
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .height(DESCRIPTION_SLOT_HEIGHT)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    EasyPanText(
                        text = stringResource(step.description),
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 20.sp),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

private fun textFadeSpec() =
    fadeIn(tween(TEXT_FADE_MILLIS)) togetherWith fadeOut(tween(TEXT_FADE_MILLIS))

@PreviewLightDark
@Composable
private fun StepPreviewCardFirstStepPreview() {
    EasyPanTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            StepPreviewCardContent(
                stepIndex = 0,
                progress = { 0f },
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun StepPreviewCardLastStepPreview() {
    EasyPanTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            StepPreviewCardContent(
                stepIndex = STEP_CARD_STEP_COUNT - 1,
                progress = { 1f },
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}
