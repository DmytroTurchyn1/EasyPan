/*
 * Created  25/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import com.cook.easypan.R
import com.cook.easypan.core.presentation.EasyPanButtonPrimary
import com.cook.easypan.core.util.GITHUB_REPOSITORY_URL
import com.cook.easypan.core.util.Launcher
import com.cook.easypan.easypan.presentation.profile.components.InformationBox
import com.cook.easypan.easypan.presentation.profile.components.SettingsItem
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun ProfileRoot(
    viewModel: ProfileViewModel,
    onSignOutButton: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    ProfileScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is ProfileAction.OnSignOut -> onSignOutButton()
                is ProfileAction.OnNotificationsClick -> {
                    Launcher.openAppSettings(context)
                }

                is ProfileAction.OnHelpClick -> {
                    Launcher.openUrl(
                        context = context,
                        url = GITHUB_REPOSITORY_URL
                    )
                }
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
private fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
) {
    Scaffold(
        topBar = {
            Text(
                text = stringResource(R.string.Profile_title),
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = innerPadding.calculateTopPadding())
                    .padding(top = 14.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .aspectRatio(1f)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = state.currentUser?.profilePictureUrl,
                        contentDescription = stringResource(R.string.profile_picture_description),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            CircularProgressIndicator()
                        },
                        error = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.user_image_description),
                                tint = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    )
                }
                Text(
                    text = state.currentUser?.username
                        ?: stringResource(R.string.username_placeholder),
                    modifier = Modifier.padding(top = 20.dp),
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        12.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InformationBox {
                        Text(
                            text = state.currentUser?.data?.recipesCooked.toString(),
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = stringResource(R.string.recipes_cooked),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.settings_title),
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp)
                )
                SettingsItem(
                    text = stringResource(R.string.keep_screen_on_title),
                    icon = Icons.Filled.ScreenLockPortrait,
                    onClick = { onAction(ProfileAction.OnKeepScreenOnToggle) },
                    clickable = false
                ) {

                    Switch(
                        checked = state.keepScreenOn,
                        onCheckedChange = { onAction(ProfileAction.OnKeepScreenOnToggle) }
                    )

                }
                SettingsItem(
                    text = stringResource(R.string.notifications_title),
                    icon = Icons.Outlined.Notifications,
                    onClick = { onAction(ProfileAction.OnNotificationsClick) },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.help_image_description),
                        tint = MaterialTheme.colorScheme.inverseSurface,
                        modifier = Modifier
                            .size(30.dp)
                            .padding(end = 6.dp)
                    )
                }
                SettingsItem(
                    text = stringResource(R.string.help_title),
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    onClick = { onAction(ProfileAction.OnHelpClick) },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.help_image_description),
                        tint = MaterialTheme.colorScheme.inverseSurface,
                        modifier = Modifier
                            .size(30.dp)
                            .padding(end = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                EasyPanButtonPrimary(
                    onClick = { onAction(ProfileAction.OnSignOut) }
                ) {
                    Text(text = stringResource(R.string.logout_button))
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    EasyPanTheme {
        ProfileScreen(
            state = ProfileState(),
            onAction = {},
        )
    }
}