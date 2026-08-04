package com.cook.easypan.easypan.data.dto

import com.cook.easypan.easypan.data.datastore.AppSettings
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

/**
 * The DataStore cache is one JSON blob covering all of [AppSettings]. A payload written before
 * ingredients gained quantities must still decode, or `ReplaceFileCorruptionHandler` resets the
 * whole file and takes the user's unrelated settings with it.
 */
class IngredientDtoSerializerTest {

    @Test
    fun `decodes a pre-migration cache without losing other settings`() {
        val legacy = """
            {
              "keepScreenOn": false,
              "userId": "user-1",
              "cachedRecipes": [
                { "id": "r1", "title": "Pancakes", "ingredients": ["Milk", "Eggs"] }
              ],
              "lastCacheTimeRecipes": 1234
            }
        """.trimIndent()

        val settings = Json.decodeFromString(AppSettings.serializer(), legacy)

        assertEquals(
            listOf(IngredientDto("Milk"), IngredientDto("Eggs")),
            settings.cachedRecipes.single().ingredients
        )
        assertEquals(false, settings.keepScreenOn)
        assertEquals("user-1", settings.userId)
        assertEquals(1234L, settings.lastCacheTimeRecipes)
    }

    @Test
    fun `decodes the current object form`() {
        val current = """
            [{ "name": "Milk", "quantity": "1/2 cup" }, { "name": "Salt", "quantity": "" }]
        """.trimIndent()

        val ingredients = Json.decodeFromString<List<IngredientDto>>(current)

        assertEquals(
            listOf(IngredientDto("Milk", "1/2 cup"), IngredientDto("Salt", "")),
            ingredients
        )
    }

    @Test
    fun `always writes the object form`() {
        val json = Json.encodeToString(listOf(IngredientDto("Milk", "1/2 cup")))

        assertEquals("""[{"name":"Milk","quantity":"1/2 cup"}]""", json)
    }

    @Test
    fun `round trips through the cache format`() {
        val original = listOf(IngredientDto("Milk", "1/2 cup"), IngredientDto("Salt"))

        val decoded = Json.decodeFromString<List<IngredientDto>>(Json.encodeToString(original))

        assertEquals(original, decoded)
    }
}
