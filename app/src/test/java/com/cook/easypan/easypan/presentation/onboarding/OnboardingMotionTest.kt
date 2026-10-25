package com.cook.easypan.easypan.presentation.onboarding

import org.junit.Test
import kotlin.test.assertEquals

class OnboardingMotionTest {

    @Test
    fun `the step walkthrough starts on the first step with an empty bar`() {
        val frame = stepFrameFor(0)

        assertEquals(0, frame.index)
        assertEquals(0f, frame.progress)
    }

    @Test
    fun `each step boundary lands on the next step and its share of the bar`() {
        repeat(STEP_CARD_STEP_COUNT) { step ->
            val frame = stepFrameFor(step * STEP_CARD_STEP_MILLIS)

            assertEquals(step, frame.index)
            assertEquals(step.toFloat() / STEP_CARD_STEP_COUNT, frame.progress, 0.0001f)
        }
    }

    @Test
    fun `the walkthrough ends on the last step with a full bar`() {
        val frame = stepFrameFor(STEP_CARD_RUN_MILLIS)

        assertEquals(STEP_CARD_STEP_COUNT - 1, frame.index)
        assertEquals(1f, frame.progress)
    }

    @Test
    fun `the step walkthrough holds its final frame instead of wrapping`() {
        val end = stepFrameFor(STEP_CARD_RUN_MILLIS)

        assertEquals(end, stepFrameFor(STEP_CARD_RUN_MILLIS + 1))
        assertEquals(end, stepFrameFor(STEP_CARD_RUN_MILLIS * 3))
    }

    @Test
    fun `the step walkthrough clamps anything before the start to the first frame`() {
        val start = stepFrameFor(0)

        assertEquals(start, stepFrameFor(-1))
        assertEquals(start, stepFrameFor(-STEP_CARD_RUN_MILLIS))
    }

    @Test
    fun `the notification stack reveals its cards one fade window apart`() {
        val fade = NOTIFICATION_STACK_FADE_IN_MILLIS

        repeat(3) { index ->
            assertEquals(0f, staggeredFraction(index * fade, index, fade), 0.0001f)
            assertEquals(1f, staggeredFraction((index + 1) * fade, index, fade), 0.0001f)
        }
    }

    @Test
    fun `a card stays hidden until its own fade window opens`() {
        val fade = NOTIFICATION_STACK_FADE_IN_MILLIS

        assertEquals(0f, staggeredFraction(fade - 1, index = 1, fade))
        assertEquals(0f, staggeredFraction(fade - 1, index = 2, fade))
        assertEquals(0f, staggeredFraction(2 * fade - 1, index = 2, fade))
    }

    @Test
    fun `every card is fully visible once the run ends and stays that way`() {
        val fade = RECIPE_CARDS_FADE_IN_MILLIS
        val run = staggerRunMillis(itemCount = 2, fadeInMillis = fade)

        repeat(2) { index ->
            assertEquals(1f, staggeredFraction(run, index, fade), 0.0001f)
            assertEquals(1f, staggeredFraction(run * 5, index, fade), 0.0001f)
        }
    }

    @Test
    fun `the run is exactly one fade window per item`() {
        assertEquals(2400, staggerRunMillis(itemCount = 3, NOTIFICATION_STACK_FADE_IN_MILLIS))
        assertEquals(1000, staggerRunMillis(itemCount = 2, RECIPE_CARDS_FADE_IN_MILLIS))
    }
}
