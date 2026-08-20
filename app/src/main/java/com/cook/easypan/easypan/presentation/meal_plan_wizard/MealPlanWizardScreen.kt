package com.cook.easypan.easypan.presentation.meal_plan_wizard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cook.easypan.R
import com.cook.easypan.core.presentation.EasyPanButtonPrimary
import com.cook.easypan.core.util.ObserveAsEvents
import com.cook.easypan.easypan.domain.model.MealPlanPreferences
import com.cook.easypan.easypan.presentation.meal_plan_wizard.wizard_steps.AllergiesContent
import com.cook.easypan.easypan.presentation.meal_plan_wizard.wizard_steps.MealIngredientsContent
import com.cook.easypan.easypan.presentation.meal_plan_wizard.wizard_steps.MealsADayContent
import com.cook.easypan.easypan.presentation.meal_plan_wizard.wizard_steps.PortionsContent
import com.cook.easypan.easypan.presentation.meal_plan_wizard.wizard_steps.SkipIngredientsContent
import com.cook.easypan.easypan.presentation.recipe_step.components.TopBarRecipeStep
import com.cook.easypan.ui.theme.EasyPanTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun MealPlanWizardRoot(
    onExit: () -> Unit,
    onFinish: (MealPlanPreferences) -> Unit,
    viewModel: MealPlanWizardViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            MealPlanWizardEvent.Exit -> onExit()
            is MealPlanWizardEvent.Finish -> onFinish(event.preferences)
        }
    }

    MealPlanWizardScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
private fun MealPlanWizardScreen(
    state: MealPlanWizardState,
    onAction: (MealPlanWizardAction) -> Unit,
) {
    BackHandler { onAction(MealPlanWizardAction.OnBackClick) }

    Scaffold(
        topBar = {
            TopBarRecipeStep(
                currentStep = state.step,
                steps = state.steps,
                progress = state.progressBar,
                onCancelClick = { onAction(MealPlanWizardAction.OnCancelClick) }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                EasyPanButtonPrimary(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canContinue,
                    onClick = { onAction(MealPlanWizardAction.OnContinueClick) }
                ) {
                    Text(
                        text = stringResource(
                            if (state.isLastStep) {
                                R.string.meal_plan_wizard_create_plan
                            } else {
                                R.string.meal_plan_wizard_continue
                            }
                        ),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                val forward = targetState > initialState
                val slide = if (forward) 1 else -1
                (slideInHorizontally(tween(300)) { width -> slide * width } + fadeIn())
                    .togetherWith(
                        slideOutHorizontally(tween(300)) { width -> -slide * width } + fadeOut()
                    )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "wizard-step"
        ) { step ->
            when (step) {
                1 -> MealIngredientsContent(state = state, onAction = onAction)
                2 -> PortionsContent(state = state, onAction = onAction)
                3 -> AllergiesContent(state = state, onAction = onAction)
                4 -> SkipIngredientsContent(state = state, onAction = onAction)
                5 -> MealsADayContent(state = state, onAction = onAction)
            }
        }
    }
}


@Preview
@Composable
private fun MealPlanWizardStep1Preview() {
    EasyPanTheme {
        MealPlanWizardScreen(
            state = MealPlanWizardState(
                step = 1,
                selectedIngredients = setOf("Quinoa", "Salad", "Stir-fry", "Hummus", "Smoothie"),
                suggestions = listOf("Avocado", "Chicken", "Salmon", "Rice", "Pasta")
            ),
            onAction = {}
        )
    }
}


@Preview
@Composable
private fun MealPlanWizardStep2Preview() {
    EasyPanTheme {
        MealPlanWizardScreen(
            state = MealPlanWizardState(step = 2, portions = 2),
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun MealPlanWizardStep3Preview() {
    EasyPanTheme {
        MealPlanWizardScreen(
            state = MealPlanWizardState(
                step = 3,
                allergies = listOf(
                    "Peanuts",
                    "Tree Nuts",
                    "Dairy",
                    "Eggs",
                    "Gluten",
                    "Soy",
                    "Sesame"
                ),
                selectedAllergies = listOf("Peanuts", "Soy")
            ),
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun MealPlanWizardStep5Preview() {
    EasyPanTheme {
        MealPlanWizardScreen(
            state = MealPlanWizardState(step = 5, selectedMeals = 3),
            onAction = {}
        )
    }
}
