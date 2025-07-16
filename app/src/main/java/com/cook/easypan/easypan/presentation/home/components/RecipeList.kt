package com.cook.easypan.easypan.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cook.easypan.easypan.domain.Recipe

/**
 * Displays a vertically scrollable list of recipes.
 *
 * Each recipe is shown as a list item. Clicking an item invokes the provided callback with the selected recipe.
 *
 * @param recipes The list of recipes to display.
 * @param onRecipeClick Callback invoked when a recipe is clicked, receiving the selected recipe.
 * @param modifier Modifier for styling and layout customization.
 * @param scrollState The scroll state for the list, allowing external control or observation of scroll position.
 */
@Composable
fun RecipeList(
    modifier: Modifier = Modifier,
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    scrollState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        state = scrollState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(
            items = recipes,
            key = { it.id }
        ) { recipe ->
            RecipeListItem(
                recipe = recipe,
                onClick = {
                    onRecipeClick(recipe)
                }
            )
        }
    }

}