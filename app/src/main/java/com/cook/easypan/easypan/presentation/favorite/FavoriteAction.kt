/*
 * Created  13/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.favorite

import com.cook.easypan.easypan.domain.model.Recipe

sealed interface FavoriteAction {
    data class OnRecipeClick(val recipe: Recipe) : FavoriteAction
}