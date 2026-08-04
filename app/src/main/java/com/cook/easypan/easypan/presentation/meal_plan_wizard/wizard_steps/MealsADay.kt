package com.cook.easypan.easypan.presentation.meal_plan_wizard.wizard_steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.cook.easypan.easypan.presentation.meal_plan_wizard.components.MealsBox
import com.cook.easypan.easypan.presentation.meal_plan_wizard.components.WizardStepHeader

private val MEAL_OPTIONS = (2..5).toList()

@Composable
fun MealsADayContent(
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