package com.cook.easypan.easypan.presentation.recipe_step

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cook.easypan.R
import com.cook.easypan.easypan.domain.StepType
import com.cook.easypan.easypan.presentation.recipe_step.cpmponents.BottomBarRecipeStep
import com.cook.easypan.easypan.presentation.recipe_step.cpmponents.TopBarRecipeStep
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
    println("recipe state: ${state.recipe}")
    if (state.recipe != null){
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopBarRecipeStep(
                    step = "${state.step}/${state.recipe.instructions.size}"
                )
            },
            bottomBar = {
                BottomBarRecipeStep(
                    onNextClick = {},
                    onPreviousClick = {}
                )
            }
        ){ innerPadding ->
            Box(
                modifier = Modifier.padding(innerPadding)
            ) {
                if (state.recipe.instructions.first().stepType == StepType.TEXT){
                    Text(
                        text = "text",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }else{
                    Text(
                        text = "timer",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
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