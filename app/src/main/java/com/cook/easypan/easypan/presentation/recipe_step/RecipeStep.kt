package com.cook.easypan.easypan.presentation.recipe_step

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.cook.easypan.R
import com.cook.easypan.easypan.domain.StepType
import com.cook.easypan.easypan.presentation.recipe_step.components.BottomBarRecipeStep
import com.cook.easypan.easypan.presentation.recipe_step.components.ContentStepRecipe
import com.cook.easypan.easypan.presentation.recipe_step.components.TimerStepRecipe
import com.cook.easypan.easypan.presentation.recipe_step.components.TopBarRecipeStep
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun RecipeStepRoot(
    viewModel: RecipeStepViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RecipeStepScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
private fun RecipeStepScreen(
    state: RecipeStepState,
    onAction: (RecipeStepAction) -> Unit,
) {
    if (state.recipe != null){
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopBarRecipeStep(
                    step = "${state.step+1} / ${state.recipe.instructions.size}",
                    progress = state.progressBar
                )
            },
            bottomBar = {
                BottomBarRecipeStep(
                    onNextClick = {onAction(RecipeStepAction.OnNextClick)},
                    onPreviousClick = {onAction(RecipeStepAction.OnPreviousClick)},
                    enabledPrevious = state.step > 0,
                    enabledNext = state.step < state.recipe.instructions.size -1
                )
            }
        ){ innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
            ) {
                AsyncImage(
                    model = state.recipe.instructions[state.step].imageUrl,
                    contentDescription = "Step Image",
                    placeholder = painterResource(R.drawable.ic_launcher_background),
                    error = painterResource(R.drawable.auth_img),
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .size(300.dp)
                )

                    if (state.recipe.instructions[state.step].stepType == StepType.TEXT){
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            ContentStepRecipe(
                                title = state.recipe.instructions[state.step].title,
                                description = state.recipe.instructions[state.step].description
                            )
                        }
                    }else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            ContentStepRecipe(
                                    title = state.recipe.instructions[state.step].title,
                                    description = state.recipe.instructions[state.step].description
                                )
                            TimerStepRecipe(
                                totalSeconds = state.recipe.instructions[state.step].durationSec,
                            )
                        }
                    }
            }
        }
    }else{
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.error_recipe)
            )
        }
    }

}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
private fun Preview() {
    EasyPanTheme {
        RecipeStepScreen(
            state = RecipeStepState(),
            onAction = {}
        )
    }
}