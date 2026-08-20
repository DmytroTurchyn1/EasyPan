package com.cook.easypan.easypan.presentation.onboarding.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cook.easypan.core.presentation.EasyPanText
import com.cook.easypan.easypan.presentation.onboarding.OnboardingPage
import com.cook.easypan.ui.theme.EasyPanTheme

private val TITLE_BLOCK_HEIGHT = 68.dp
private val TEXT_HORIZONTAL_PADDING = 24.dp

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OnboardingHero(image = page.image)

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

@PreviewLightDark
@Composable
private fun OnboardingPageContentPreview() {
    EasyPanTheme {
        OnboardingPageContent(page = OnboardingPage.TIMERS)
    }
}
