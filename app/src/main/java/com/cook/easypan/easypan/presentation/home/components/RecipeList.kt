/*
 * Created  22/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.home.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.cook.easypan.easypan.domain.model.Recipe

@Composable
fun RecipeList(
    modifier: Modifier = Modifier,
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    scrollState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        state = scrollState,
        modifier = modifier
            .testTag("recipe_list"),
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