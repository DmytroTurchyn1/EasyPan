package com.cook.easypan.easypan.presentation.meal_plan_wizard.wizard_steps

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.cook.easypan.easypan.presentation.meal_plan_wizard.components.AllergiesItem
import com.cook.easypan.easypan.presentation.meal_plan_wizard.components.WizardStepHeader

@Composable
fun AllergiesContent(
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