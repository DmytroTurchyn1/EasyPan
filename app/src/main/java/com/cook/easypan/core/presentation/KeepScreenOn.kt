/*
 * Created  13/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.core.presentation

import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
fun KeepScreenOn() {
    val activity = LocalActivity.current
    DisposableEffect(Unit) {
        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}