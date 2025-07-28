package com.cook.easypan.easypan.presentation.recipe_finish

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cook.easypan.R
import com.cook.easypan.core.presentation.EasyPanButtonPrimary
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun RecipeFinishRoot(
    viewModel: RecipeFinishViewModel,
    onFinishClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RecipeFinishScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is RecipeFinishAction.OnFinishClick -> onFinishClick()
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
private fun RecipeFinishScreen(
    state: RecipeFinishState,
    onAction: (RecipeFinishAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.recipe_finished_title),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            fontSize = 36.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(64.dp))
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = stringResource(R.string.recipe_finished_image),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.FillBounds,
        )
        Spacer(modifier = Modifier.height(58.dp))
        EasyPanButtonPrimary(
            onClick = { onAction(RecipeFinishAction.OnFinishClick) },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.recipe_finished_button),
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    EasyPanTheme {
        RecipeFinishScreen(
            state = RecipeFinishState(),
            onAction = {}
        )
    }
}