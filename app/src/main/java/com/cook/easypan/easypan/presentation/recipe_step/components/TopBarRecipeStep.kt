package com.cook.easypan.easypan.presentation.recipe_step.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cook.easypan.R
import com.cook.easypan.easypan.presentation.recipe_step.RecipeStepAction
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun TopBarRecipeStep(
    step: String,
    progress: Float
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically

        ) {
            IconButton(
                onClick = { RecipeStepAction.OnBackClick }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.go_back),
                    tint = Color.White
                )

            }
            Text(
                text = "${stringResource(R.string.step)} $step",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .height(10.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }


}

@Preview
@Composable
private fun TopBarRecipeStepPreview() {
    EasyPanTheme {
        TopBarRecipeStep(
            step = "1",
            progress = 0.2f
        )
    }

}