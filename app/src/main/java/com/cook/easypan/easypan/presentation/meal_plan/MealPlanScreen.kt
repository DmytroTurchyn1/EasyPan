package com.cook.easypan.easypan.presentation.meal_plan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import com.cook.easypan.R
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.core.presentation.EasyPanButtonPrimary
import com.cook.easypan.core.presentation.toMessageRes
import com.cook.easypan.core.util.ObserveAsEvents
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.presentation.meal_plan.components.DayChip
import com.cook.easypan.easypan.presentation.meal_plan.components.RecipeItem
import com.cook.easypan.ui.theme.EasyPanTheme

@Composable
fun MealPlanRoot(
    viewModel: MealPlanViewModel,
    onOpenWizard: () -> Unit,
    onRecipeClick: (Recipe) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            MealPlanEvent.OpenWizard -> onOpenWizard()
            is MealPlanEvent.OpenRecipe -> onRecipeClick(event.recipe)
        }
    }

    MealPlanScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
private fun MealPlanScreen(
    state: MealPlanState,
    onAction: (MealPlanAction) -> Unit,
) {
    when {
        state.isLoading -> Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        state.error != null -> ErrorContent(
            error = state.error,
            onRetry = { onAction(MealPlanAction.OnRetry) },
        )

        !state.hasPlan -> MealPlanPlaceHolderScreen(
            state = state,
            onAction = onAction,
        )

        else -> MealPlanCreatedScreen(
            state = state,
            onAction = onAction,
        )
    }
}

@Composable
private fun MealPlanCreatedScreen(
    state: MealPlanState,
    onAction: (MealPlanAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.meal_plan_tab_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        R.string.meal_plan_tab_subtitle,
                        state.weekRange,
                        state.people,
                        state.mealsDay,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = { onAction(MealPlanAction.OnRegenerateClick) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.meal_plan_regenerate_description),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(
                items = state.days,
                key = { _, day -> day.dayOfMonth },
            ) { index, day ->
                DayChip(
                    dayAbbrev = day.dayAbbrev,
                    dayOfMonth = day.dayOfMonth,
                    isSelected = index == state.selectedDayIndex,
                    onClick = { onAction(MealPlanAction.OnDaySelected(index)) },
                )
            }
        }

        val selectedDay = state.selectedDay
        if (selectedDay != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedDay.dayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                TextButton(onClick = { onAction(MealPlanAction.OnEditClick) }) {
                    Text(
                        text = stringResource(R.string.meal_plan_review_edit),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                selectedDay.meals.forEach { recipe ->
                    RecipeItem(
                        recipe = recipe,
                        onClick = { onAction(MealPlanAction.OnRecipeClick(recipe)) },
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        state.upNextDay?.let { upNext ->
            UpNextCard(
                day = upNext,
                onClick = { onAction(MealPlanAction.OnDaySelected(state.selectedDayIndex + 1)) },
                modifier = Modifier.padding(vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun UpNextCard(
    day: MealPlanDayUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.meal_plan_up_next, day.dayName),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = day.meals.joinToString(" · ") { it.title },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            day.meals.take(3).forEach { recipe ->
                SubcomposeAsyncImage(
                    model = recipe.titleImg,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    error = {
                        Image(
                            painter = painterResource(R.drawable.auth_img),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    },
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Default.NavigateNext,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProIncludedRow(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text(
                text = stringResource(R.string.meal_plan_pro_badge),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.meal_plan_pro_included),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
@Composable
private fun MealPlanPlaceHolderScreen(
    state: MealPlanState,
    onAction: (MealPlanAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.meal_plan_title),
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(106.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.RestaurantMenu,
                    contentDescription = stringResource(R.string.meal_plan_icon_description),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(60.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.meal_plan_headline),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.meal_plan_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(28.dp))

            EasyPanButtonPrimary(
                onClick = { onAction(MealPlanAction.OnCreatePlanClick) },
            ) {

            Text(
                    text = stringResource(R.string.meal_plan_create_button),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
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
private fun ErrorContent(
    error: AppError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
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
}

private val previewRecipe = Recipe(
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

@Preview(showBackground = true)
@Composable
private fun MealPlanCreatedScreenPreview() {
    EasyPanTheme {
        MealPlanScreen(
            state = MealPlanState(
                weekRange = "Jul 13 – 19",
                people = 2,
                mealsDay = 3,
                days = listOf(
                    MealPlanDayUi("Monday", "Mon", 13, List(3) { previewRecipe }),
                    MealPlanDayUi("Tuesday", "Tue", 14, List(3) { previewRecipe }),
                    MealPlanDayUi("Wednesday", "Wed", 15, List(3) { previewRecipe }),
                ),
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MealPlanPlaceholderScreenPreview() {
    EasyPanTheme {
        MealPlanScreen(
            state = MealPlanState(),
            onAction = {},
        )
    }
}
