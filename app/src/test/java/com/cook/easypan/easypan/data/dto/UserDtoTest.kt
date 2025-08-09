package com.cook.easypan.easypan.data.dto

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class UserDtoTest {

    @Test
    fun `test UserDto default values`() {
        val recipeDto = UserDto()

        assertEquals(0, recipeDto.recipesCooked)
    }

    @Test
    fun `test UserDto custom values`() {
        val recipeDto = UserDto(3)

        assertEquals(3, recipeDto.recipesCooked)
    }

    @Test
    fun `test PropertyName annotation works correctly`() {
        val propertyAnnotation = UserDto::class.java
            .getDeclaredField("recipesCooked")
            .getAnnotation(PropertyName::class.java)

        assertEquals("recipesCooked", propertyAnnotation!!.value)
    }

    @Test
    fun `test serialization and deserialization`() {
        val original = UserDto(recipesCooked = 5)
        val json = Json.encodeToString(UserDto.serializer(), original)
        val deserialized = Json.decodeFromString(UserDto.serializer(), json)

        assertEquals(original, deserialized)
        assertEquals(5, deserialized.recipesCooked)
    }
}