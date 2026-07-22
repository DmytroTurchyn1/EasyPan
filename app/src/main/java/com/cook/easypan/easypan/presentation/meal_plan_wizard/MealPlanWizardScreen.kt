package com.cook.easypan.easypan.presentation.meal_plan_wizard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cook.easypan.R
import com.cook.easypan.core.presentation.EasyPanButtonPrimary
import com.cook.easypan.core.util.ObserveAsEvents
import com.cook.easypan.easypan.domain.model.MealPlanPreferences
import com.cook.easypan.easypan.presentation.meal_plan_wizard.components.AllergiesItem
import com.cook.easypan.easypan.presentation.meal_plan_wizard.components.IngredientChip
import com.cook.easypan.easypan.presentation.meal_plan_wizard.components.MealsBox
import com.cook.easypan.easypan.presentation.meal_plan_wizard.components.PortionTile
import com.cook.easypan.easypan.presentation.meal_plan_wizard.components.SearchIngredientField
import com.cook.easypan.easypan.presentation.meal_plan_wizard.components.WizardStepHeader
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
                1 -> Step1Content(state = state, onAction = onAction)
                2 -> Step2Content(state = state, onAction = onAction)
                3 -> Step3Content(state = state, onAction = onAction)
                4 -> Step4Content(state = state, onAction = onAction)
                5 -> Step5Content(state = state, onAction = onAction)
                else -> ComingSoonContent()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step1Content(
    state: MealPlanWizardState,
    onAction: (MealPlanWizardAction) -> Unit,
) {
    val visibleSuggestions = state.suggestions.filter { ingredient ->
        ingredient !in state.selectedIngredients &&
                ingredient.contains(state.searchQuery.trim(), ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WizardStepHeader(
            title = stringResource(R.string.meal_plan_wizard_q1_title),
            subtitle = stringResource(R.string.meal_plan_wizard_q1_subtitle)
        )

        Spacer(modifier = Modifier.height(24.dp))

        SearchIngredientField(
            value = state.searchQuery,
            onValueChange = { onAction(MealPlanWizardAction.OnSearchQueryChange(it)) }
        )

        if (state.selectedIngredients.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            SectionLabel(
                text = stringResource(
                    R.string.meal_plan_wizard_selected,
                    state.selectedIngredients.size
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.selectedIngredients.forEach { ingredient ->
                    IngredientChip(
                        text = ingredient,
                        selected = true,
                        onClick = {
                            onAction(MealPlanWizardAction.OnIngredientToggle(ingredient))
                        }
                    )
                }
            }
        }

        if (visibleSuggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            SectionLabel(text = stringResource(R.string.meal_plan_wizard_suggestions))

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                visibleSuggestions.forEach { ingredient ->
                    IngredientChip(
                        text = ingredient,
                        selected = false,
                        onClick = {
                            onAction(MealPlanWizardAction.OnIngredientToggle(ingredient))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun Step2Content(
    state: MealPlanWizardState,
    onAction: (MealPlanWizardAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WizardStepHeader(
            title = stringResource(R.string.meal_plan_wizard_title_step2),
            subtitle = stringResource(R.string.meal_plan_wizard_subtitle_step2)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PORTION_OPTIONS.chunked(3).forEach { rowNumbers ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowNumbers.forEach { number ->
                        PortionTile(
                            number = number,
                            selected = state.portions == number,
                            onClick = { onAction(MealPlanWizardAction.OnPortionsSelect(number)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun Step3Content(
    state: MealPlanWizardState,
    onAction: (MealPlanWizardAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WizardStepHeader(
            title = stringResource(R.string.meal_plan_wizard_title_step3),
            subtitle = stringResource(R.string.meal_plan_wizard_subtitle_step3)
        )

        Spacer(modifier = Modifier.height(24.dp))

        state.allergies.forEach { allergy ->
            AllergiesItem(
                text = allergy,
                onCheckClick = { onAction(MealPlanWizardAction.OnAllergyCheck(allergy)) },
                checked = allergy in state.selectedAllergies
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step4Content(
    state: MealPlanWizardState,
    onAction: (MealPlanWizardAction) -> Unit,
) {
    val visibleSuggestions = state.suggestions.filter { ingredient ->
        ingredient !in state.selectedIngredientsSkip &&
                ingredient.contains(state.searchQuery.trim(), ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WizardStepHeader(
            title = stringResource(R.string.meal_plan_wizard_q4_title),
            subtitle = stringResource(R.string.meal_plan_wizard_q4_subtitle)
        )

        Spacer(modifier = Modifier.height(24.dp))

        SearchIngredientField(
            value = state.searchQuery,
            onValueChange = { onAction(MealPlanWizardAction.OnSearchQueryChange(it)) }
        )

        if (state.selectedIngredientsSkip.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            SectionLabel(
                text = stringResource(
                    R.string.meal_plan_wizard_selected,
                    state.selectedIngredientsSkip.size
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.selectedIngredientsSkip.forEach { ingredient ->
                    IngredientChip(
                        text = ingredient,
                        selected = true,
                        onClick = {
                            onAction(MealPlanWizardAction.OnIngredientSkipToggle(ingredient))
                        }
                    )
                }
            }
        }

        if (visibleSuggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            SectionLabel(text = stringResource(R.string.meal_plan_wizard_suggestions))

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                visibleSuggestions.forEach { ingredient ->
                    IngredientChip(
                        text = ingredient,
                        selected = false,
                        onClick = {
                            onAction(MealPlanWizardAction.OnIngredientSkipToggle(ingredient))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun Step5Content(
    state: MealPlanWizardState,
    onAction: (MealPlanWizardAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WizardStepHeader(
            title = stringResource(R.string.meal_plan_wizard_q5_title),
            subtitle = stringResource(R.string.meal_plan_wizard_q5_subtitle)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MEAL_OPTIONS.forEach { number ->
                MealsBox(
                    number = number,
                    subtitle = when (number) {
                        2 -> stringResource(R.string.meal_plan_wizard_meals_2_subtitle)
                        3 -> stringResource(R.string.meal_plan_wizard_meals_3_subtitle)
                        else -> stringResource(R.string.meal_plan_wizard_meals_snack_subtitle)
                    },
                    selected = state.selectedMeals == number,
                    onClick = { onAction(MealPlanWizardAction.OnMealsSelect(number)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ComingSoonContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.meal_plan_wizard_coming_soon),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.fillMaxWidth()
    )
}

private val PORTION_OPTIONS = (1..6).toList()
private val MEAL_OPTIONS = (2..5).toList()

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
