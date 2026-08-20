/*
 * Created  20/8/2026
 *
 * Copyright (c) 2026 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.recipe_finish


sealed interface RecipeFinishEvent {
    data object RequestReview : RecipeFinishEvent
}