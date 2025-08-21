/*
 * Created  21/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.core.util


import java.util.Locale


fun formatMsToMS(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0L)
    val minutes = s / 60
    val seconds = s % 60
    return String.format(locale = Locale.ENGLISH, "%02d:%02d", minutes, seconds)
}