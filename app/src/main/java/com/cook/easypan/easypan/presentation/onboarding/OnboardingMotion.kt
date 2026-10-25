package com.cook.easypan.easypan.presentation.onboarding

const val STEP_CARD_STEP_COUNT = 6
const val STEP_CARD_STEP_MILLIS = 900
const val STEP_CARD_RUN_MILLIS = STEP_CARD_STEP_COUNT * STEP_CARD_STEP_MILLIS

const val NOTIFICATION_STACK_FADE_IN_MILLIS = 800
const val RECIPE_CARDS_FADE_IN_MILLIS = 500
const val WEEK_STRIP_FADE_IN_MILLIS = 120

data class StepFrame(val index: Int, val progress: Float)

fun stepFrameFor(elapsedMillis: Int): StepFrame {
    val time = elapsedMillis.coerceIn(0, STEP_CARD_RUN_MILLIS)
    if (time == STEP_CARD_RUN_MILLIS) {
        return StepFrame(index = STEP_CARD_STEP_COUNT - 1, progress = 1f)
    }
    val index = time / STEP_CARD_STEP_MILLIS
    val withinStep = (time % STEP_CARD_STEP_MILLIS).toFloat() / STEP_CARD_STEP_MILLIS
    return StepFrame(
        index = index,
        progress = (index + withinStep) / STEP_CARD_STEP_COUNT
    )
}

fun staggerRunMillis(itemCount: Int, fadeInMillis: Int): Int = itemCount * fadeInMillis

fun staggeredFraction(elapsedMillis: Int, index: Int, fadeInMillis: Int): Float {
    val start = index * fadeInMillis
    return ((elapsedMillis - start).toFloat() / fadeInMillis).coerceIn(0f, 1f)
}
