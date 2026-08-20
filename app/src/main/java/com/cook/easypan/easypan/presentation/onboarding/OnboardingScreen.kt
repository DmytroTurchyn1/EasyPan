package com.cook.easypan.easypan.presentation.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cook.easypan.R
import com.cook.easypan.core.presentation.EasyPanButtonPrimary
import com.cook.easypan.core.presentation.EasyPanText
import com.cook.easypan.core.util.ObserveAsEvents
import com.cook.easypan.easypan.presentation.onboarding.components.OnboardingPageContent
import com.cook.easypan.easypan.presentation.onboarding.components.OnboardingPageIndicator
import com.cook.easypan.ui.theme.EasyPanTheme
import org.koin.androidx.compose.koinViewModel

private val CONTENT_HORIZONTAL_PADDING = 24.dp
private val BUTTON_HEIGHT = 48.dp
private val TOP_BAR_HEIGHT = 48.dp

@Composable
fun OnboardingRoot(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            OnboardingEvent.Finish -> onFinished()
        }
    }

    OnboardingScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
private fun OnboardingScreen(
    state: OnboardingState,
    onAction: (OnboardingAction) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = state.currentPage,
        pageCount = { state.pageCount }
    )

    LaunchedEffect(state.currentPage) {
        if (pagerState.currentPage != state.currentPage) {
            pagerState.animateScrollToPage(state.currentPage)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page -> onAction(OnboardingAction.OnPageChanged(page)) }
    }

    BackHandler(enabled = state.currentPage > 0) {
        onAction(OnboardingAction.OnBackClick)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .heightIn(min = TOP_BAR_HEIGHT),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (!state.isLastPage) {
                    TextButton(onClick = { onAction(OnboardingAction.OnSkipClick) }) {
                        EasyPanText(
                            text = stringResource(R.string.onboarding_skip),
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = CONTENT_HORIZONTAL_PADDING)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                OnboardingPageIndicator(
                    pageCount = state.pageCount,
                    currentPage = state.currentPage
                )
                EasyPanButtonPrimary(
                    onClick = { onAction(OnboardingAction.OnContinueClick) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(BUTTON_HEIGHT)
                ) {
                    EasyPanText(
                        text = stringResource(R.string.onboarding_continue),
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalAlignment = Alignment.Top
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                OnboardingPageContent(page = state.pages[page])
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun OnboardingScreenFirstPagePreview() {
    EasyPanTheme {
        OnboardingScreen(
            state = OnboardingState(currentPage = 0),
            onAction = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun OnboardingScreenLastPagePreview() {
    EasyPanTheme {
        OnboardingScreen(
            state = OnboardingState(currentPage = 4),
            onAction = {}
        )
    }
}
