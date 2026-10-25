package com.cook.easypan.easypan.presentation.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cook.easypan.core.presentation.EasyPanText
import com.cook.easypan.easypan.presentation.onboarding.OnboardingPage
import com.cook.easypan.easypan.presentation.onboarding.OnboardingVisual
import com.cook.easypan.ui.theme.EasyPanTheme
import kotlin.math.abs

private val TITLE_BLOCK_HEIGHT = 68.dp
private val TEXT_HORIZONTAL_PADDING = 24.dp
private val VISUAL_SLOT_HEIGHT = 352.dp
private val STEP_CARD_TOP_PADDING = 16.dp
private val ENTRANCE_RISE = 16.dp
private val WEEK_STRIP_SPACING = 26.dp

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    isActive: Boolean,
    pageOffset: () -> Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (page.visual) {
            OnboardingVisual.HERO -> OnboardingHero(
                image = requireNotNull(page.image),
                pageOffset = pageOffset
            )

            OnboardingVisual.STEP_CARD -> VisualSlot(
                alignment = Alignment.TopCenter,
                topPadding = STEP_CARD_TOP_PADDING
            ) {
                StepPreviewCard(isActive = isActive)
            }

            OnboardingVisual.NOTIFICATION_STACK -> VisualSlot {
                NotificationPreviewStack(isActive = isActive)
            }

            OnboardingVisual.RECIPE_CARDS -> VisualSlot(alignment = Alignment.TopCenter) {
                Column(verticalArrangement = Arrangement.spacedBy(WEEK_STRIP_SPACING)) {
                    WeekPreviewStrip(isActive = isActive)
                    RecipePreviewCards(isActive = isActive)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                // The text settles as the page does: reading pageOffset() inside graphicsLayer keeps
                // the whole entrance in the draw phase.
                .graphicsLayer {
                    val settle = (1f - abs(pageOffset())).coerceIn(0f, 1f)
                    alpha = settle
                    translationY = (1f - settle) * ENTRANCE_RISE.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = TITLE_BLOCK_HEIGHT)
                    .padding(horizontal = TEXT_HORIZONTAL_PADDING),
                contentAlignment = Alignment.Center
            ) {
                EasyPanText(
                    text = stringResource(page.title),
                    style = MaterialTheme.typography.displaySmall.copy(lineHeight = 34.sp),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            EasyPanText(
                text = stringResource(page.description),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TEXT_HORIZONTAL_PADDING)
            )
        }
    }
}

@Composable
private fun VisualSlot(
    alignment: Alignment = Alignment.Center,
    topPadding: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(VISUAL_SLOT_HEIGHT)
            .padding(horizontal = TEXT_HORIZONTAL_PADDING)
            .padding(top = topPadding),
        contentAlignment = alignment
    ) {
        content()
    }
}

@PreviewLightDark
@Composable
private fun OnboardingPageContentStepCardPreview() {
    EasyPanTheme {
        OnboardingPageContent(page = OnboardingPage.ONE_STEP, isActive = true, pageOffset = { 0f })
    }
}


@PreviewLightDark
@Composable
private fun OnboardingPageContentNotificationsPreview() {
    EasyPanTheme {
        OnboardingPageContent(
            page = OnboardingPage.NOTIFICATIONS,
            isActive = true,
            pageOffset = { 0f })
    }
}

@PreviewLightDark
@Composable
private fun OnboardingPageContentWeekSortedPreview() {
    EasyPanTheme {
        OnboardingPageContent(
            page = OnboardingPage.WEEK_SORTED,
            isActive = true,
            pageOffset = { 0f })
    }
}
