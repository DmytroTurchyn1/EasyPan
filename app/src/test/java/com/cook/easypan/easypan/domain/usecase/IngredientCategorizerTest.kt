package com.cook.easypan.easypan.domain.usecase

import com.cook.easypan.easypan.domain.model.IngredientCategory
import org.junit.Test
import kotlin.test.assertEquals

class IngredientCategorizerTest {

    @Test
    fun `plain ingredient names map to their category`() {
        assertEquals(IngredientCategory.MEAT_FISH, IngredientCategorizer.categorize("Chicken"))
        assertEquals(IngredientCategory.PRODUCE, IngredientCategorizer.categorize("Spinach"))
        assertEquals(IngredientCategory.DAIRY_EGGS, IngredientCategorizer.categorize("Eggs"))
        assertEquals(IngredientCategory.PANTRY, IngredientCategorizer.categorize("Rice"))
    }

    @Test
    fun `matches by substring inside free-text amounts`() {
        assertEquals(
            IngredientCategory.PANTRY,
            IngredientCategorizer.categorize("2 cups all-purpose flour"),
        )
        assertEquals(
            IngredientCategory.DAIRY_EGGS,
            IngredientCategorizer.categorize("1¾ cups buttermilk"),
        )
        assertEquals(
            IngredientCategory.MEAT_FISH,
            IngredientCategorizer.categorize("500 g chicken breast"),
        )
    }

    @Test
    fun `matching is case-insensitive`() {
        assertEquals(IngredientCategory.PRODUCE, IngredientCategorizer.categorize("SPINACH"))
        assertEquals(IngredientCategory.PANTRY, IngredientCategorizer.categorize("olive OIL"))
    }

    @Test
    fun `longer keywords win over shorter overlapping ones`() {
        // "bell pepper" (produce) must beat "pepper" (pantry spice)…
        assertEquals(IngredientCategory.PRODUCE, IngredientCategorizer.categorize("Bell Peppers"))
        // …while plain "black pepper" stays pantry…
        assertEquals(IngredientCategory.PANTRY, IngredientCategorizer.categorize("black pepper"))
        // …and "eggplant" (produce) must beat "egg" (dairy).
        assertEquals(IngredientCategory.PRODUCE, IngredientCategorizer.categorize("Eggplant"))
    }

    @Test
    fun `unknown ingredients fall back to OTHER`() {
        assertEquals(IngredientCategory.OTHER, IngredientCategorizer.categorize("Hummus"))
        assertEquals(IngredientCategory.OTHER, IngredientCategorizer.categorize("Smoothie"))
        assertEquals(IngredientCategory.OTHER, IngredientCategorizer.categorize(""))
    }
}
