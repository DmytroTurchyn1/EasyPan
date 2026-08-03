package com.cook.easypan.easypan.presentation.ingredients_receipt

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import com.cook.easypan.easypan.domain.model.GroceryItem
import com.cook.easypan.easypan.presentation.recipe_detail.components.IngredientsItem
import com.cook.easypan.ui.theme.EasyPanTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun IngredientsListRoot(
    onCreateShoppingList: () -> Unit,
    viewModel: IngredientsReceiptViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    IngredientsListScreen(
        state = state,
        onAction = viewModel::onAction,
        onCreateShoppingList = onCreateShoppingList,
    )
}

/**
 * "What do you have?" — the step before the receipt. Every ingredient the plan needs is listed
 * flat; whatever the user checks off is something they already own, so it drops off the receipt
 * that comes next.
 */
@Composable
fun IngredientsListScreen(
    state: IngredientsReceiptState,
    onAction: (IngredientsReceiptAction) -> Unit,
    onCreateShoppingList: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(R.string.ingredients_list_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.ingredients_list_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onCreateShoppingList,
                    content = {
                        Text(
                            text = stringResource(R.string.ingredients_list_continue_button),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
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

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                ) {
                    items(
                        items = state.ingredients,
                        key = { ingredient -> ingredient.name },
                    ) { ingredient ->
                        IngredientsItem(
                            text = ingredient.name,
                            quantity = ingredient.quantity,
                            checked = ingredient.name in state.checkedIngredients,
                            onCheckClick = {
                                onAction(IngredientsReceiptAction.OnCheckClick(ingredient.name))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun IngredientsListScreenPreview() {
    EasyPanTheme {
        IngredientsListScreen(
            state = IngredientsReceiptState(
                isLoading = false,
                ingredients = listOf(
                    GroceryItem("Spaghetti", "320 g"),
                    GroceryItem("Penne", "250 g"),
                    GroceryItem("Fusilli", "300 g"),
                    GroceryItem("Linguine", "280 g"),
                    GroceryItem("Macaroni", "200 g"),
                    GroceryItem("Tagliatelle", "350 g"),
                    GroceryItem("Rigatoni", "300 g"),
                ),
                checkedIngredients = setOf("Spaghetti", "Penne", "Fusilli", "Macaroni"),
            ),
            onAction = {},
            onCreateShoppingList = {},
        )
    }
}
