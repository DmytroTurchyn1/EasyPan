/*
 * Created  8/8/2026
 *
 * Copyright (c) 2026 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.data.analytics

import android.content.Context
import android.util.Log
import com.amplitude.android.Amplitude
import com.amplitude.android.AutocaptureOption
import com.amplitude.android.Configuration

class AnalyticsClient(
    context: Context,
    apiKey: String,
) {

    companion object {
        private const val TAG = "AnalyticsClient"
    }

    private val amplitude: Amplitude? = if (apiKey.isBlank()) {
        Log.e(
            TAG,
            "AMPLITUDE_API_KEY is blank — add amplitude to keystore.properties. " +
                    "Analytics events will be dropped."
        )
        null
    } else {
        Amplitude(
            Configuration(
                apiKey = apiKey,
                context = context,
                autocapture = setOf(
                    AutocaptureOption.SESSIONS,
                    AutocaptureOption.APP_LIFECYCLES,
                    AutocaptureOption.SCREEN_VIEWS,
                ),
            )
        )
    }

    fun track(event: String, properties: Map<String, Any?> = emptyMap()) {
        amplitude?.track(event, properties.filterValues { it != null })
    }

    fun setUserId(userId: String?) {
        amplitude?.setUserId(userId)
    }

    fun reset() {
        amplitude?.reset()
    }
}
