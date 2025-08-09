package com.cook.easypan.easypan.data.mappers

import com.cook.easypan.easypan.data.dto.UserDto
import com.cook.easypan.easypan.domain.model.UserData
import org.junit.Assert.assertEquals
import org.junit.Test

class UserMappersTest {

    @Test
    fun `toUserData mapping valid UserDto to UserData`() {
        val userDto = UserDto(recipesCooked = 5)

        val result = userDto.toUserData()

        assertEquals(5, result.recipesCooked)
    }

    @Test
    fun `toUserData mapping UserDto with default recipesCooked`() {
        val userDto = UserDto()

        val result = userDto.toUserData()

        assertEquals(0, result.recipesCooked)
    }

    @Test
    fun `toUserDto mapping valid UserData to UserDto`() {
        val userData = UserData(recipesCooked = 10)

        val result = userData.toUserDto()

        assertEquals(10, result.recipesCooked)
    }

    @Test
    fun `toUserDto mapping UserData with default recipesCooked`() {
        val userData = UserData()

        val result = userData.toUserDto()

        assertEquals(0, result.recipesCooked)
    }

    @Test
    fun `toUserData with zero recipesCooked`() {
        val userDto = UserDto(recipesCooked = 0)

        val result = userDto.toUserData()

        assertEquals(0, result.recipesCooked)
    }

    @Test
    fun `toUserData with large recipesCooked value`() {
        val userDto = UserDto(recipesCooked = 999)

        val result = userDto.toUserData()

        assertEquals(999, result.recipesCooked)
    }

    @Test
    fun `toUserDto with large recipesCooked value`() {
        val userData = UserData(recipesCooked = 999)

        val result = userData.toUserDto()

        assertEquals(999, result.recipesCooked)
    }

    @Test
    fun `mapping preserves data`() {
        val originalUserData = UserData(recipesCooked = 42)

        val convertedToDto = originalUserData.toUserDto()
        val backToUserData = convertedToDto.toUserData()

        assertEquals(originalUserData.recipesCooked, backToUserData.recipesCooked)
    }

    @Test
    fun `reverse mapping preserves data`() {
        val originalUserDto = UserDto(recipesCooked = 24)

        val convertedToUserData = originalUserDto.toUserData()
        val backToDto = convertedToUserData.toUserDto()

        assertEquals(originalUserDto.recipesCooked, backToDto.recipesCooked)
    }

    @Test
    fun `mapping works with different values`() {
        val testValues = listOf(0, 1, 5, 25, 100, 500)

        testValues.forEach { value ->
            val userDto = UserDto(recipesCooked = value)
            val userData = UserData(recipesCooked = value)

            assertEquals(value, userDto.toUserData().recipesCooked)
            assertEquals(value, userData.toUserDto().recipesCooked)
        }
    }
}