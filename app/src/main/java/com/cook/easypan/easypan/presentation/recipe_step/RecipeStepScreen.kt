package com.cook.easypan.easypan.presentation.recipe_step

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import coil3.compose.SubcomposeAsyncImage
import com.cook.easypan.R
import com.cook.easypan.easypan.domain.StepType
import com.cook.easypan.easypan.presentation.recipe_step.components.AlertCancelRecipeDialog
import com.cook.easypan.easypan.presentation.recipe_step.components.BottomBarRecipeStep
import com.cook.easypan.easypan.presentation.recipe_step.components.ContentStepRecipe
import com.cook.easypan.easypan.presentation.recipe_step.components.TimerStepRecipe
import com.cook.easypan.easypan.presentation.recipe_step.components.TopBarRecipeStep
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun RecipeStepRoot(
    viewModel: RecipeStepViewModel,
    onFinishClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RecipeStepScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is RecipeStepAction.OnFinishClick -> onFinishClick()
                is RecipeStepAction.OnCancelClick -> onCancelClick()
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
private fun RecipeStepScreen(
    state: RecipeStepState,
    onAction: (RecipeStepAction) -> Unit,
) {

    if (state.isDialogShowing) {
        AlertCancelRecipeDialog(
            icon = Icons.Default.Info,
            onConfirmation = {
                onAction(RecipeStepAction.OnCancelClick)
            },
            onDismissRequest = { onAction(RecipeStepAction.OnDismissDialog) },
            dialogTitle = stringResource(R.string.cancel_recipe_dialog_title),
            dialogText = stringResource(R.string.cancel_recipe_dialog_text),
        )
    }

    if (state.recipe != null) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopBarRecipeStep(
                    currentStep = state.step + 1,
                    steps = state.recipe.instructions.size,
                    progress = state.progressBar,
                    onCancelClick = { onAction(RecipeStepAction.OnShowDialog) },
                )
            },
            bottomBar = {
                BottomBarRecipeStep(
                    onNextClick = {
                        if (state.step < state.recipe.instructions.size - 1) {
                            onAction(RecipeStepAction.OnNextClick)
                        } else {
                            onAction(RecipeStepAction.OnNextClick)
                            onAction(RecipeStepAction.OnFinishClick)
                        }
                    },
                    onPreviousClick = { onAction(RecipeStepAction.OnPreviousClick) },
                    enabledPrevious = state.step > 0,
                    nextButtonTitle = if (state.step < state.recipe.instructions.size - 1) stringResource(
                        R.string.next_button
                    ) else stringResource(R.string.finish_button)
                )
            }
        ) { innerPadding ->
            AnimatedContent(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                targetState = state.step,
                label = "StepContent",
                transitionSpec = {
                    if (targetState > initialState) {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) togetherWith
                                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
                    } else {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right) togetherWith
                                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                    }
                }
            ) { currentStep ->
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                            .size(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SubcomposeAsyncImage(
                            model = state.recipe.instructions[currentStep].imageUrl,
                            contentDescription = stringResource(R.string.dish_image_description),
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            },
                            error = {
                                Image(
                                    painter = painterResource(R.drawable.auth_img),
                                    contentDescription = null,
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        )
                    }
                    if (state.recipe.instructions[currentStep].stepType == StepType.TEXT) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            ContentStepRecipe(
                                title = state.recipe.instructions[currentStep].title,
                                description = state.recipe.instructions[currentStep].description
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),

                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            ContentStepRecipe(
                                title = state.recipe.instructions[currentStep].title,
                                description = state.recipe.instructions[currentStep].description
                            )
                            TimerStepRecipe(
                                totalSeconds = state.recipe.instructions[currentStep].durationSec,
                            )
                        }
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.error_recipe),
                color = MaterialTheme.colorScheme.onSurface
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