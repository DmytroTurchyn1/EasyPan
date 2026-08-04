/*
 * Created  18/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.core.domain

sealed interface Result {
    data object Success : Result
    data class Failure(val error: AppError) : Result
}

enum class AppError {
    SIGN_IN_CANCELLED,
    NO_GOOGLE_ACCOUNT,
    AUTH_FAILED,
    NOT_SIGNED_IN,
    REAUTH_REQUIRED,
    NETWORK,
    UNKNOWN
}
