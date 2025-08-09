package com.cook.easypan.easypan.domain.model

import org.junit.Assert
import org.junit.Test

class UserDataTest {

    @Test
    fun `test user data not empty`() {
        val userData = UserData(recipesCooked = 5)

        Assert.assertEquals(5, userData.recipesCooked)
    }

    @Test
    fun `test user data  empty`() {
        val userData = UserData()

        Assert.assertEquals(0, userData.recipesCooked)
    }
}