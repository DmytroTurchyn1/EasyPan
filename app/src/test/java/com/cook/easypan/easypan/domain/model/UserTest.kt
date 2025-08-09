package com.cook.easypan.easypan.domain.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserTest {

    @Test
    fun `test User`() {
        val user = User(
            userId = "testUserId",
            username = "test",
            profilePictureUrl = null,
            data = null
        )

        assertEquals("testUserId", user.userId)
        assertEquals("test", user.username)
        assertNull(user.profilePictureUrl)
        assertNull(user.data)
    }

    @Test
    fun `user should use default values when not provided`() {
        val defaultUser = User()

        assertEquals("", defaultUser.userId)
        assertNull(defaultUser.username)
        assertNull(defaultUser.profilePictureUrl)
        assertNull(defaultUser.data)
    }


    @Test
    fun `user should handle all fields populated`() {

        val userData = UserData()

        val user = User(
            userId = "user123",
            username = "JohnDoe",
            profilePictureUrl = "https://example.com/profile.jpg",
            data = userData
        )

        assertEquals("user123", user.userId)
        assertEquals("JohnDoe", user.username)
        assertEquals("https://example.com/profile.jpg", user.profilePictureUrl)
        assertEquals(userData, user.data)
    }

    @Test
    fun `user should handle all fields populated and check user data`() {

        val userData = UserData(recipesCooked = 1)

        val user = User(
            userId = "user123",
            username = "JohnDoe",
            profilePictureUrl = "https://example.com/profile.jpg",
            data = userData
        )

        assertEquals("user123", user.userId)
        assertEquals("JohnDoe", user.username)
        assertEquals("https://example.com/profile.jpg", user.profilePictureUrl)
        assertEquals(userData, user.data)
        assertEquals(1, user.data?.recipesCooked)
    }
}