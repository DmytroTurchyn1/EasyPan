package com.cook.easypan.easypan.presentation.profile

import ProfileAction
import ProfileState
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.cook.easypan.R
import com.cook.easypan.easypan.domain.UserData
import com.cook.easypan.easypan.presentation.profile.components.InformationBox
import com.cook.easypan.easypan.presentation.profile.components.SettingsItem
import com.cook.easypan.ui.theme.EasyPanTheme


/**
 * Entry point composable for the profile screen, connecting UI state and actions to the ProfileViewModel.
 *
 * Collects the current profile state from the view model and displays the profile UI. Handles user actions by forwarding them to the view model and triggers the sign-out callback when requested.
 *
 * @param viewModel The ProfileViewModel providing profile state and handling actions.
 * @param onSingOutButton Callback invoked when the user requests to sign out.
 * @param userData The current user's data, or null if unavailable.
 */
@Composable
fun ProfileRoot(
    viewModel: ProfileViewModel,
    onSingOutButton: () -> Unit,
    userData: UserData?
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is ProfileAction.OnSignOut -> onSingOutButton()
                else -> Unit
            }
            viewModel.onAction(action)
        },
        userData = userData
    )
}

/**
 * Displays the user profile screen with profile information, statistics, and settings options.
 *
 * Shows the user's profile picture, username, number of recipes cooked, favorite cuisines, and provides settings for notifications, help & support, and sign-out.
 *
 * @param state The current profile state containing user statistics and settings.
 * @param onAction Callback invoked when a profile-related action is triggered by the user.
 * @param userData The user's profile data, including username and profile picture URL, or null if unavailable.
 */
@Composable
private fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
    userData: UserData?
) {
    EasyPanTheme {
        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.Profile_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp
                    )
                }

            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(userData?.profilePictureUrl)
                        .crossfade(true)
                        .listener(
                            onError = { _, result ->
                                Log.e("Profile", "Image load failed", result.throwable)
                            }
                        )
                        .build(),
                    placeholder = painterResource(R.drawable.ic_launcher_background),
                    error = painterResource(R.drawable.auth_img),
                    contentDescription = "profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = userData?.username ?: "username",
                    modifier = Modifier.padding(top = 20.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp

                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InformationBox {
                        Text(
                            text = "${state.recipesCooked}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.recipes_cooked)
                        )
                    }
                    InformationBox {
                        Text(
                            text = "${state.favoriteCuisines}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.favorite_cuisines)
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(16.dp))
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp)
                )
                SettingsItem(
                    text = "Notifications",
                    icon = Icons.Outlined.Notifications,
                    onClick = { onAction(ProfileAction.OnNotificationsToggle) },
                ) {
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = { onAction(ProfileAction.OnNotificationsToggle) }
                    )
                }
                SettingsItem(
                    text = "Help & Support",
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    onClick = { onAction },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Navigate to help",
                        modifier = Modifier
                            .size(30.dp)
                            .padding(end = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.padding(15.dp))
                Button(
                    onClick = { onAction(ProfileAction.OnSignOut) }
                ) {
                    Text(text = stringResource(R.string.logout_button))
                }
            }
        }
    }
}

/**
 * Displays a preview of the profile screen with sample user data and default state for design-time visualization.
 */
@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    EasyPanTheme {
        ProfileScreen(
            state = ProfileState(),
            onAction = {},
            userData = UserData(
                userId = "12345",
                username = "JohnDoe",
                profilePictureUrl = null
            )
        )
    }
}