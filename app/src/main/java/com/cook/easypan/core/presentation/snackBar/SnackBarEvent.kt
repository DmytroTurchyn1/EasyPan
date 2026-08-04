/*
 * Created  16/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.core.presentation.snackBar

import androidx.annotation.StringRes

data class SnackBarEvent(
    val message: String? = null,
    @param:StringRes val messageRes: Int? = null,
    val action: SnackBarAction? = null
)
