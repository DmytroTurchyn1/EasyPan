/*
 * Created  13/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.recipe_finish

import com.cook.easypan.easypan.domain.model.Recipe

sealed interface RecipeFinishAction {
    data object OnFinishClick : RecipeFinishAction
    data class OnRecipeChange(val recipe: Recipe) : RecipeFinishAction
    data class OnFavoriteClick(val recipe: Recipe) : RecipeFinishAction

}