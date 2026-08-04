package com.cook.easypan.easypan.presentation.meal_plan_wizard.wizard_steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cook.easypan.R
import com.cook.easypan.easypan.presentation.meal_plan_wizard.MealPlanWizardAction
import com.cook.easypan.easypan.presentation.meal_plan_wizard.MealPlanWizardState
import com.cook.easypan.easypan.presentation.meal_plan_wizard.components.IngredientChip
import com.cook.easypan.easypan.presentation.meal_plan_wizard.components.SearchIngredientField
import com.cook.easypan.easypan.presentation.meal_plan_wizard.components.SectionLabel
import com.cook.easypan.easypan.presentation.meal_plan_wizard.components.WizardStepHeader

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MealIngredientsContent(
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