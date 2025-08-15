package com.cook.easypan.easypan.domain.model

import com.cook.easypan.core.domain.StepType
import org.junit.Assert
import kotlin.test.Test

class StepDescriptionTest {

    @Test
    fun `test StepDescription not empty`() {
        val stepDescription = StepDescription(
            step = 1,
            imageUrl = "https://example.com/image.jpg",
            title = "Chop Vegetables",
            description = "Chop all the vegetables into small pieces.",
            stepType = StepType.TEXT,
            durationSec = 300
        )

        Assert.assertEquals(1, stepDescription.step)
        Assert.assertEquals("https://example.com/image.jpg", stepDescription.imageUrl)
        Assert.assertEquals("Chop Vegetables", stepDescription.title)
        Assert.assertEquals(
            "Chop all the vegetables into small pieces.",
            stepDescription.description
        )
        Assert.assertEquals(StepType.TEXT, stepDescription.stepType)
        Assert.assertEquals(300, stepDescription.durationSec)
    }

    @Test
    fun `test StepDescription imageUrl and durationSec empty`() {
        val stepDescription = StepDescription(
            step = 1,
            title = "Chop Vegetables",
            description = "Chop all the vegetables into small pieces.",
            stepType = StepType.TEXT
        )

        Assert.assertNull(stepDescription.imageUrl)
        Assert.assertNull(stepDescription.durationSec)
    }

}