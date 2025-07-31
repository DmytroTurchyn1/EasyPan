package com.cook.easypan.easypan.presentation.authentication

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cook.easypan.R
import com.cook.easypan.core.presentation.EasyPanButtonPrimary
import com.cook.easypan.ui.theme.EasyPanTheme

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.auth_img),
                contentDescription = "Logo",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.012f)
            )
            Text(
                text = stringResource(R.string.auth_title),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(14.dp),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp
            )
            Text(
                text = stringResource(R.string.auth_description),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(14.dp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.weight(0.01f))

            EasyPanButtonPrimary(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = {
                    onAction(AuthenticationAction.OnAuthButtonClick)
                },
                enabled = !state.isLoading
            ) {
                Row(
                    modifier = Modifier
                        .padding(top = 2.dp, bottom = 2.dp)
                        .weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,


                    ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.google_icon),
                        contentDescription = stringResource(R.string.google_icon_description),
                    )
                    Text(
                        text = stringResource(R.string.continue_with_google),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(start = 12.dp)
                    )
                }
            }
            Text(
                text = stringResource(R.string.terms_and_conditions),
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 26.dp, start = 5.dp, end = 5.dp)
            )
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
        val context = LocalContext.current
        LaunchedEffect(state.signInError, context) {
            if (state.signInError != null) {
                Toast.makeText(context, state.signInError, Toast.LENGTH_SHORT).show()
            }
        }

    }
}

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    EasyPanTheme {
        AuthenticationScreen(
            state = AuthenticationState(),
            onAction = {}
        )
    }
}