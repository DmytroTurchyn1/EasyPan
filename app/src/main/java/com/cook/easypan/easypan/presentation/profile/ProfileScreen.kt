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
import androidx.compose.material3.ButtonColors
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


@Composable
fun ProfileRoot(
    viewModel: ProfileViewModel,
    onSignOutButton: () -> Unit,
    userData: UserData?
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is ProfileAction.OnSignOut -> onSignOutButton()
                else -> Unit
            }
            viewModel.onAction(action)
        },
        userData = userData
    )
}

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
                    contentDescription = stringResource(R.string.profile_picture_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = userData?.username ?: stringResource(R.string.username_placeholder),
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
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = stringResource(R.string.recipes_cooked),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    InformationBox {
                        Text(
                            text = "${state.favoriteCuisines}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = stringResource(R.string.favorite_cuisines),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
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
                    text = stringResource(R.string.notifications_title),
                    icon = Icons.Outlined.Notifications,
                    onClick = { onAction(ProfileAction.OnNotificationsToggle) },
                ) {
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = { onAction(ProfileAction.OnNotificationsToggle) }
                    )
                }
                SettingsItem(
                    text = stringResource(R.string.help_title),
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    onClick = { onAction },
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
                Spacer(modifier = Modifier.padding(15.dp))
                Button(
                    onClick = { onAction(ProfileAction.OnSignOut) },
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContentColor = MaterialTheme.colorScheme.surfaceBright,
                        disabledContainerColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.5f)
                    )
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
            userData = UserData(
                userId = "12345",
                username = "JohnDoe",
                profilePictureUrl = null
            )
        )
    }
}