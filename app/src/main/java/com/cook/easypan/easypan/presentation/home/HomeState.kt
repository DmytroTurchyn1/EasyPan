/*
 * Created  14/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.home

import androidx.compose.foundation.lazy.LazyListState
import com.cook.easypan.easypan.domain.model.Recipe

data class HomeState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val recipeListState: LazyListState = LazyListState(),
    val hasSubscribedToTopic: Boolean = false,
    val filterList: List<String> = emptyList(),
    val selectedFilter: String = "",
)