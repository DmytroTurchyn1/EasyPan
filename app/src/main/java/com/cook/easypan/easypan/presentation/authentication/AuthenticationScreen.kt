/*
 * Created  16/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.authentication

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cook.easypan.R
import com.cook.easypan.core.presentation.snackBar.SnackBarController
import com.cook.easypan.core.presentation.snackBar.SnackBarEvent
import com.cook.easypan.core.presentation.toMessageRes
import com.cook.easypan.easypan.presentation.authentication.components.GoogleSignInButton
import com.cook.easypan.ui.theme.EasyPanTheme

private val HERO_HEIGHT = 320.dp
private val SCRIM_OFFSET = 262.dp
private val SCRIM_HEIGHT = 90.dp

@Composable
fun AuthenticationRoot(
    viewModel: AuthenticationViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AuthenticationScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
private fun AuthenticationScreen(
    state: AuthenticationState,
    onAction: (AuthenticationAction) -> Unit,
) {
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GoogleSignInButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onAction(AuthenticationAction.OnAuthButtonClick(context))
                    },
                    enabled = !state.isLoading
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = termsAndPrivacyText(),
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 21.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    ) { innerPadding ->
        state.signInError?.let { error ->
            val errorMessage = stringResource(error.toMessageRes())
            LaunchedEffect(error) {
                SnackBarController.sendEvent(
                    event = SnackBarEvent(message = errorMessage)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeroImage()
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(R.string.auth_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(42.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureRow(text = stringResource(R.string.auth_feature_guided_cooking))
                    FeatureRow(text = stringResource(R.string.auth_feature_matching_recipes))
                    FeatureRow(text = stringResource(R.string.auth_feature_meal_plan))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            val animatedColor by animateColorAsState(
                targetValue = if (state.isLoading) Color.Black.copy(alpha = 0.8f) else Color.Transparent,
                label = "overlay_color"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(animatedColor)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun HeroImage(
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
            painter = painterResource(id = R.drawable.auth_img),
            contentDescription = stringResource(R.string.auth_image_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Fades the bottom of the illustration into the page background. The scrim starts partway
        // down the hero and is taller than the space left below it, so its last 32dp are clipped —
        // this is what keeps the counter edge visible instead of fading it out early.
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

@Composable
private fun FeatureRow(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * "By continuing, you agree to our Terms and Privacy Policy" with the two legal labels highlighted.
 * The labels are styled but not tappable — the app has no policy URLs to link to yet.
 */
@Composable
private fun termsAndPrivacyText(): AnnotatedString {
    val terms = stringResource(R.string.auth_terms_link)
    val privacy = stringResource(R.string.auth_privacy_link)
    val full = stringResource(R.string.auth_terms_full, terms, privacy)
    val linkColor = MaterialTheme.colorScheme.tertiary
    return remember(full, linkColor) {
        buildAnnotatedString {
            append(full)
            listOf(terms, privacy).forEach { label ->
                val start = full.indexOf(label)
                if (start >= 0) {
                    addStyle(SpanStyle(color = linkColor), start, start + label.length)
                }
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AuthenticationScreenPreview() {
    EasyPanTheme {
        AuthenticationScreen(
            state = AuthenticationState(),
            onAction = {}
        )
    }
}
