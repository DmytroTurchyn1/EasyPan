package com.cook.easypan.easypan.presentation.meal_plan_wizard.wizard_steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.cook.easypan.easypan.presentation.meal_plan_wizard.components.PortionTile
import com.cook.easypan.easypan.presentation.meal_plan_wizard.components.WizardStepHeader

private val PORTION_OPTIONS = (1..6).toList()

@Composable
fun PortionsContent(
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