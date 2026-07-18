package com.cook.easypan.easypan.presentation.meal_plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cook.easypan.R
import com.cook.easypan.core.presentation.EasyPanButtonPrimary
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun MealPlanRoot(
    viewModel: MealPlanViewModel,
    onCreatePlanClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MealPlanScreen(
        state = state,
        onAction = { action ->
            when (action) {
                MealPlanAction.OnCreatePlanClick -> onCreatePlanClick()
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
private fun MealPlanScreen(
    state: MealPlanState,
    onAction: (MealPlanAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.meal_plan_title),
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(106.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RestaurantMenu,
                    contentDescription = stringResource(R.string.meal_plan_icon_description),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.meal_plan_headline),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.meal_plan_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            EasyPanButtonPrimary(
                onClick = { onAction(MealPlanAction.OnCreatePlanClick) }
            ) {
                Text(
                    text = stringResource(R.string.meal_plan_create_button),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            if (!state.isProUser) {
                Spacer(modifier = Modifier.height(16.dp))
                ProIncludedRow()
            }
        }
    }
}

@Composable
private fun ProIncludedRow(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = stringResource(R.string.meal_plan_pro_badge),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.meal_plan_pro_included),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MealPlanScreenPreview() {
    EasyPanTheme {
        MealPlanScreen(
            state = MealPlanState(),
            onAction = {}
        )
    }
}
