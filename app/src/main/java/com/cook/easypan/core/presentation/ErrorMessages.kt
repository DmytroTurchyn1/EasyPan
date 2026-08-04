/*
 * Created  2/7/2026
 *
 * Copyright (c) 2026 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.core.presentation

import androidx.annotation.StringRes
import com.cook.easypan.R
import com.cook.easypan.core.domain.AppError

@StringRes
fun AppError.toMessageRes(): Int = when (this) {
    AppError.SIGN_IN_CANCELLED -> R.string.error_sign_in_failed
    AppError.NO_GOOGLE_ACCOUNT -> R.string.error_no_google_account
    AppError.AUTH_FAILED -> R.string.error_sign_in_failed
    AppError.NOT_SIGNED_IN -> R.string.error_not_signed_in
    AppError.REAUTH_REQUIRED -> R.string.error_delete_account
    AppError.NETWORK -> R.string.error_network
    AppError.UNKNOWN -> R.string.error_unknown
}
