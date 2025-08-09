package com.cook.easypan.easypan.data.dto

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class StepDescriptionDtoTest {

    private lateinit var stepDescriptionDto: StepDescriptionDto

    @Before
    fun setup() {
        stepDescriptionDto = StepDescriptionDto(
            step = 1,
            imageUrl = "test_image_url",
            title = "Test Title",
            description = "Test Description",
            stepType = "Text",
            durationSec = 100
        )
    }

    @Test
    fun `test StepDescriptionDto default values`() {
        val emptyStepDescriptionDto = StepDescriptionDto()

        assertEquals("", emptyStepDescriptionDto.description)
        assertEquals("", emptyStepDescriptionDto.title)
        assertEquals("text", emptyStepDescriptionDto.stepType)
        assertEquals(0, emptyStepDescriptionDto.step)
        assertNull(emptyStepDescriptionDto.imageUrl)
        assertNull(emptyStepDescriptionDto.durationSec)

    }

    @Test
    fun `test StepDescriptionDto custom values`() {

        assertEquals("Test Description", stepDescriptionDto.description)
        assertEquals("Test Title", stepDescriptionDto.title)
        assertEquals("Text", stepDescriptionDto.stepType)
        assertEquals("test_image_url", stepDescriptionDto.imageUrl)
        assertEquals(1, stepDescriptionDto.step)
        assertEquals(100, stepDescriptionDto.durationSec)
    }

    @Test
    fun `test all PropertyName annotations are correctly set`() {
        val expectedMappings = mapOf(
            "step" to "step",
            "imageUrl" to "imageUrl",
            "title" to "title",
            "description" to "description",
            "stepType" to "stepType",
            "durationSec" to "durationSec"
        )

        expectedMappings.forEach { (fieldName, expectedValue) ->
            val propertyAnnotation = StepDescriptionDto::class.java
                .getDeclaredField(fieldName)
                .getAnnotation(PropertyName::class.java)

            assertNotNull("PropertyName annotation missing for $fieldName", propertyAnnotation)
            assertEquals(
                "PropertyName value incorrect for $fieldName",
                expectedValue, propertyAnnotation!!.value
            )
        }
    }

    @Test
    fun `test serialization and deserialization`() {

        val json = Json.encodeToString(StepDescriptionDto.serializer(), stepDescriptionDto)
        val deserialized = Json.decodeFromString(StepDescriptionDto.serializer(), json)

        assertEquals(stepDescriptionDto, deserialized)
        assertEquals("Test Description", deserialized.description)
        assertEquals("Test Title", deserialized.title)
        assertEquals("Text", deserialized.stepType)
        assertEquals("test_image_url", deserialized.imageUrl)
        assertEquals(1, deserialized.step)
        assertEquals(100, deserialized.durationSec)
    }

}