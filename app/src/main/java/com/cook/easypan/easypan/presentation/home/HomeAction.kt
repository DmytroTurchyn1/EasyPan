/*
 * Created  14/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.home

import com.cook.easypan.easypan.domain.model.Recipe

sealed interface HomeAction {
    data class OnRecipeClick(val recipe: Recipe) : HomeAction
    data class OnFilterSelected(val filter: String) : HomeAction
}