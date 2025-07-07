package com.dmytro_turchyn.easypan.easypan.presentation.profile

import ProfileAction
import ProfileState
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.dmytro_turchyn.easypan.R
import com.dmytro_turchyn.easypan.easypan.domain.UserData
import com.dmytro_turchyn.easypan.easypan.presentation.profile.components.InformationBox
import com.dmytro_turchyn.easypan.ui.theme.EasyPanTheme

@Composable
fun ProfileRoot(
    viewModel: ProfileViewModel = viewModel<ProfileViewModel>(),
    userData: UserData?
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        onAction = viewModel::onAction,
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
                Box (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp
                    )
                }

            }
        ) { innerPadding ->
            println("picture: ${userData?.profilePictureUrl}")
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
                    horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    InformationBox{
                        Text(
                            text = 125.toString(),
                        )
                        Text(
                            text = "Recipes Cooked"
                        )
                    }
                    InformationBox{
                        Text(
                            text = 5.toString(),
                        )
                        Text(
                            text = "Favorite Cuisines"
                        )
                    }
                }
                Text(
                    text = "Settings"
                )
                Button(
                    onClick = { onAction(ProfileAction.OnSignOut) }
                ) {
                    Text(text = "log out")
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