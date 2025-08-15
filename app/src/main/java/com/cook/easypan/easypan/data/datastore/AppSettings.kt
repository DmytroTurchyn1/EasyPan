/*
 * Created  15/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.data.datastore

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val keepScreenOn: Boolean = true
)