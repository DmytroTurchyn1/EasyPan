package com.cook.easypan.easypan.presentation.meal_plan_review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.core.presentation.EasyPanButtonPrimary
import com.cook.easypan.core.presentation.toMessageRes
import com.cook.easypan.core.util.ObserveAsEvents
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.presentation.meal_plan_review.components.DayElement
import com.cook.easypan.easypan.presentation.meal_plan_review.components.PlanPreferenceChip
import com.cook.easypan.ui.theme.EasyPanTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun MealPlanReviewRoot(
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onContinue: () -> Unit,
    onRecipeClick: (Recipe) -> Unit,
    viewModel: MealPlanReviewViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            MealPlanReviewEvent.Continue -> onContinue()
            MealPlanReviewEvent.Dismiss -> onDismiss()
            MealPlanReviewEvent.EditPlan -> onEdit()
            is MealPlanReviewEvent.OpenRecipe -> onRecipeClick(event.recipe)
        }
    }

    MealPlanReviewScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
fun MealPlanReviewScreen(
    state: MealPlanReviewState,
    onAction: (MealPlanReviewAction) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!state.isLoading && state.error == null) {
                MealPlanReviewButtons(onAction = onAction)
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                )

                state.error != null -> ErrorContent(
                    error = state.error,
                    onRetry = { onAction(MealPlanReviewAction.OnRetry) },
                    modifier = Modifier.align(Alignment.Center),
                )

                else -> MealPlanContent(state = state, onAction = onAction)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MealPlanContent(
    state: MealPlanReviewState,
    onAction: (MealPlanReviewAction) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 24.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                IconButton(onClick = { onAction(MealPlanReviewAction.OnDismissPlanClick) }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.go_back),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.meal_plan_review_title),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (state.weekRange.isNotEmpty()) {
                        Text(
                            text = state.weekRange,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                PlanPreferenceChip(
                    text = stringResource(R.string.meal_plan_review_chip_people, state.people),
                )
                PlanPreferenceChip(
                    text = stringResource(R.string.meal_plan_review_chip_meals, state.mealsDay),
                )
                state.allergies.forEach { allergy ->
                    PlanPreferenceChip(
                        text = stringResource(
                            R.string.meal_plan_review_chip_no_allergy,
                            allergy.lowercase(),
                        ),
                    )
                }
            }
        }
        items(state.days, key = { it.dateLabel }) { day ->
            DayElement(
                dayName = day.dayName,
                dateLabel = day.dateLabel,
                meals = day.meals,
                onRecipeClick = { onAction(MealPlanReviewAction.OnRecipeClick(it)) },
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun MealPlanReviewButtons(
    onAction: (MealPlanReviewAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EasyPanButtonPrimary(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onAction(MealPlanReviewAction.OnContinueClick) },
        ) {
            Text(
                text = stringResource(R.string.meal_plan_review_continue),
                fontWeight = FontWeight.Bold,
            )
        }
        TextButton(onClick = { onAction(MealPlanReviewAction.OnEditClick) }) {
            Text(
                text = stringResource(R.string.meal_plan_review_edit),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: AppError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(error.toMessageRes()),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        EasyPanButtonPrimary(onClick = onRetry) {
            Text(text = stringResource(R.string.retry_button))
        }
    }
}

@Preview
@Composable
private fun MealPlanReviewScreenPreview() {
    val sampleRecipe = Recipe(
        id = "1",
        title = "Spicy Chicken Stir-Fry",
        ingredients = listOf("Chicken", "Rice"),
        allergies = emptyList(),
        preparationMinutes = 10,
        cookMinutes = 30,
        chips = emptyList(),
        difficulty = "Medium",
        instructions = emptyList(),
        titleImg = "",
    )
    EasyPanTheme {
        MealPlanReviewScreen(
            state = MealPlanReviewState(
                isLoading = false,
                weekRange = "Jul 13 – 19",
                people = 2,
                mealsDay = 3,
                allergies = listOf("Peanuts"),
                days = listOf(
                    PlanDayUi("Monday", "Jul 13", List(3) { sampleRecipe }),
                    PlanDayUi("Tuesday", "Jul 14", List(3) { sampleRecipe }),
                ),
            ),
            onAction = {},
        )
    }
}
