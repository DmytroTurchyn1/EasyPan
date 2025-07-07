package com.dmytro_turchyn.easypan.easypan.presentation.profile

import ProfileAction
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dmytro_turchyn.easypan.R
import com.dmytro_turchyn.easypan.easypan.domain.UserData
import com.dmytro_turchyn.easypan.easypan.presentation.profile.components.InformationBox
import com.dmytro_turchyn.easypan.ui.theme.EasyPanTheme

@Composable
fun ProfileRoot(
    viewModel: ProfileViewModel,
    userData: UserData?
) {
    ProfileScreen(
        onAction = viewModel::onAction,
        userData = userData
    )
}

@Composable
private fun ProfileScreen(
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
                        text = "Profile",
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
                if (userData?.profilePictureUrl != null) {
                    AsyncImage(
                        model = userData.profilePictureUrl,
                        contentDescription = "profile picture",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.auth_img),
                        contentDescription = "profile picture",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = userData?.username ?: "username",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )


                Spacer(modifier = Modifier.height(32.dp))

               Row(
                     horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                     verticalAlignment = Alignment.CenterVertically,
                     modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
               ) {
                   InformationBox {
                       Text(
                           text = "125",
                           style = MaterialTheme.typography.bodyMedium
                       )
                       Text(
                           text = "Recipes Cooked",
                           style = MaterialTheme.typography.bodyMedium
                       )
                   }
                   InformationBox {
                       Text(
                           text = "5",
                           style = MaterialTheme.typography.bodyMedium
                       )
                       Text(
                           text = "Favorite Cuisines",
                           style = MaterialTheme.typography.bodyMedium
                       )
                   }
               }


                Spacer(modifier = Modifier.height(32.dp))
                Text("Settings")
                // Sign Out Button
                Button(
                    onClick = { onAction(ProfileAction.OnSignOut) },
                ) {
                    Text("Sign Out")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen(
        onAction = {},
        userData = UserData(
            userId = "1",
            username = "John Doe",
            profilePictureUrl = null
        ),

    )
}