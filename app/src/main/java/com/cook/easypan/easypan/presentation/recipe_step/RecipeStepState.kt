/*
 * Created  25/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.presentation.recipe_step

import com.cook.easypan.easypan.domain.model.Recipe

data class RecipeStepState(
    val recipe: Recipe? = null,
    val step: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val progressBar: Float = 0f,
    val isDialogShowing: Boolean = false,
    val isFinishButtonEnabled: Boolean = true,
    val keepScreenOn: Boolean = true,
    val timerRunning: Boolean = false,
)