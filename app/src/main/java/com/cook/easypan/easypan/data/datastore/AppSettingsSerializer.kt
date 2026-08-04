/*
 * Created  14/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object AppSettingsSerializer : Serializer<AppSettings> {
    override val defaultValue: AppSettings
        get() = AppSettings()

    override suspend fun readFrom(input: InputStream): AppSettings {
        val bytes = input.readBytes()
        // An empty file simply means nothing has been written yet.
        if (bytes.isEmpty()) return defaultValue
        return try {
            Json.Default.decodeFromString(
                deserializer = AppSettings.serializer(),
                string = bytes.decodeToString()
            )
        } catch (e: SerializationException) {
            // Surfaced to the ReplaceFileCorruptionHandler configured on the
            // DataStore, which resets the file to defaults.
            throw CorruptionException("Cannot deserialize AppSettings", e)
        } catch (e: IllegalArgumentException) {
            throw CorruptionException("Cannot deserialize AppSettings", e)
        }
    }

    override suspend fun writeTo(
        t: AppSettings,
        output: OutputStream
    ) {
        output.write(
            Json.Default.encodeToString(
                serializer = AppSettings.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }
}
