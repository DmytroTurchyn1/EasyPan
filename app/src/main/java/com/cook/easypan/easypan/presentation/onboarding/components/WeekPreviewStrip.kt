package com.cook.easypan.easypan.presentation.onboarding.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.cook.easypan.R
import com.cook.easypan.core.presentation.rememberAnimationsEnabled
import com.cook.easypan.easypan.presentation.meal_plan.components.DayChip
import com.cook.easypan.easypan.presentation.onboarding.WEEK_STRIP_FADE_IN_MILLIS
import com.cook.easypan.easypan.presentation.onboarding.staggerRunMillis
import com.cook.easypan.easypan.presentation.onboarding.staggeredFraction
import com.cook.easypan.ui.theme.EasyPanTheme

private const val SELECTED_DAY_INDEX = 0

private data class PreviewDay(
    @param:StringRes val abbrev: Int,
    val dayOfMonth: Int,
)

private val PREVIEW_DAYS = listOf(
    PreviewDay(abbrev = R.string.onboarding_week_day_1, dayOfMonth = 13),
    PreviewDay(abbrev = R.string.onboarding_week_day_2, dayOfMonth = 14),
    PreviewDay(abbrev = R.string.onboarding_week_day_3, dayOfMonth = 15),
    PreviewDay(abbrev = R.string.onboarding_week_day_4, dayOfMonth = 16),
    PreviewDay(abbrev = R.string.onboarding_week_day_5, dayOfMonth = 17),
    PreviewDay(abbrev = R.string.onboarding_week_day_6, dayOfMonth = 18),
    PreviewDay(abbrev = R.string.onboarding_week_day_7, dayOfMonth = 19),
)

@Composable
fun WeekPreviewStrip(
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.onboarding_week_strip_description)
    val elapsed = rememberOneShotElapsed(
        durationMillis = staggerRunMillis(PREVIEW_DAYS.size, WEEK_STRIP_FADE_IN_MILLIS),
        isActive = isActive,
        enabled = rememberAnimationsEnabled()
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PREVIEW_DAYS.forEachIndexed { index, day ->
            WeekPreviewDay(
                day = day,
                isSelected = index == SELECTED_DAY_INDEX,
                alpha = {
                    LinearOutSlowInEasing.transform(
                        staggeredFraction(
                            elapsedMillis = elapsed.value.toInt(),
                            index = index,
                            fadeInMillis = WEEK_STRIP_FADE_IN_MILLIS
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun WeekPreviewDay(
    day: PreviewDay,
    isSelected: Boolean,
    alpha: () -> Float,
    modifier: Modifier = Modifier,
) {
    DayChip(
        dayAbbrev = stringResource(day.abbrev),
        dayOfMonth = day.dayOfMonth,
        isSelected = isSelected,
        modifier = modifier.graphicsLayer { this.alpha = alpha() }
    )
}

@PreviewLightDark
@Composable
private fun WeekPreviewStripPreview() {
    EasyPanTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PREVIEW_DAYS.forEachIndexed { index, day ->
                    DayChip(
                        dayAbbrev = stringResource(day.abbrev),
                        dayOfMonth = day.dayOfMonth,
                        isSelected = index == SELECTED_DAY_INDEX
                    )
                }
            }
        }
    }
}
