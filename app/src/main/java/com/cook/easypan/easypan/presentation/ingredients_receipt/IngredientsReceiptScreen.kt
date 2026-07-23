package com.cook.easypan.easypan.presentation.ingredients_receipt

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cook.easypan.R
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.core.presentation.EasyPanButtonPrimary
import com.cook.easypan.core.presentation.toMessageRes
import com.cook.easypan.easypan.domain.model.GroceryCategory
import com.cook.easypan.easypan.domain.model.IngredientCategory
import com.cook.easypan.easypan.presentation.ingredients_receipt.components.GroceriesReceipt
import com.cook.easypan.easypan.presentation.ingredients_receipt.components.ReceiptCategory
import com.cook.easypan.easypan.presentation.ingredients_receipt.components.ReceiptItem
import com.cook.easypan.ui.theme.EasyPanTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun IngredientsReceiptRoot(
    viewModel: IngredientsReceiptViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    IngredientsReceiptScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun IngredientsReceiptScreen(
    state: IngredientsReceiptState,
    onAction: (IngredientsReceiptAction) -> Unit,
) {
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(R.string.ingredients_list_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.ingredients_list_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Start
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EasyPanButtonPrimary(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = { onAction(IngredientsReceiptAction.OnShareButtonClick) },
                    content = {
                        Text(
                            text = stringResource(R.string.ingredients_list_share_button),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                TextButton(onClick = { onAction(IngredientsReceiptAction.OnEditClick) }) {
                    Text(
                        text = stringResource(R.string.ingredients_list_continue_button),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                }

            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                )

                state.error != null -> ErrorContent(
                    error = state.error,
                    onRetry = { onAction(IngredientsReceiptAction.OnRetry) },
                    modifier = Modifier.align(Alignment.Center),
                )

                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                ) {
                    GroceriesReceipt(
                        meta = stringResource(
                            R.string.ingredients_list_receipt_meta,
                            state.mealsCount,
                            state.people,
                        ),
                        categories = state.categories.toReceiptCategories(),
                        itemCount = stringResource(
                            R.string.ingredients_list_receipt_items,
                            state.totalItems,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun List<GroceryCategory>.toReceiptCategories(): List<ReceiptCategory> = map { group ->
    ReceiptCategory(
        name = stringResource(group.category.toLabelRes()),
        items = group.items.map { ReceiptItem(name = it) },
    )
}

private fun IngredientCategory.toLabelRes(): Int = when (this) {
    IngredientCategory.PRODUCE -> R.string.ingredients_list_receipt_cat_produce
    IngredientCategory.MEAT_FISH -> R.string.ingredients_list_receipt_cat_meat
    IngredientCategory.DAIRY_EGGS -> R.string.ingredients_list_receipt_cat_dairy
    IngredientCategory.PANTRY -> R.string.ingredients_list_receipt_cat_pantry
    IngredientCategory.OTHER -> R.string.ingredients_list_receipt_cat_other
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
private fun IngredientsReceiptScreenPreview() {
    EasyPanTheme {
        IngredientsReceiptScreen(
            state = IngredientsReceiptState(
                isLoading = false,
                mealsCount = 21,
                people = 2,
                totalItems = 8,
                categories = listOf(
                    GroceryCategory(
                        category = IngredientCategory.PRODUCE,
                        items = listOf("Bell Peppers", "Broccoli", "Spinach"),
                    ),
                    GroceryCategory(
                        category = IngredientCategory.MEAT_FISH,
                        items = listOf("Chicken", "Salmon"),
                    ),
                    GroceryCategory(
                        category = IngredientCategory.PANTRY,
                        items = listOf("2 cups all-purpose flour", "Rice"),
                    ),
                    GroceryCategory(
                        category = IngredientCategory.OTHER,
                        items = listOf("Hummus"),
                    ),
                ),
            ),
            onAction = {}
        )
    }
}
