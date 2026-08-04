package com.cook.easypan.easypan.domain.usecase

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class QuantityMergerTest {

    @Test
    fun `sums amounts sharing a unit`() {
        assertEquals("300 g", QuantityMerger.merge(listOf("100 g", "200 g")))
    }

    @Test
    fun `sums identical amounts instead of collapsing them`() {
        assertEquals("400 g", QuantityMerger.merge(listOf("200 g", "200 g")))
    }

    @Test
    fun `parses an amount written without a space`() {
        assertEquals("300 g", QuantityMerger.merge(listOf("100g", "200g")))
    }

    @Test
    fun `normalises plurals and synonyms before summing`() {
        assertEquals("3 cups", QuantityMerger.merge(listOf("1 cup", "2 cups")))
        assertEquals("300 g", QuantityMerger.merge(listOf("100 grams", "200 gr")))
        assertEquals("3 tbsp", QuantityMerger.merge(listOf("1 tablespoon", "2 tablespoons")))
    }

    @Test
    fun `sums fractions and renders the total as a mixed number`() {
        assertEquals("3/4 cup", QuantityMerger.merge(listOf("1/2 cup", "1/4 cup")))
        assertEquals("2 1/2 cups", QuantityMerger.merge(listOf("1/2 cup", "2 cups")))
        assertEquals("1 cup", QuantityMerger.merge(listOf("1/2 cup", "1/2 cup")))
    }

    @Test
    fun `reads mixed numbers and unicode fractions`() {
        assertEquals("3 cups", QuantityMerger.merge(listOf("1 1/2 cups", "1 1/2 cups")))
        assertEquals("1 cup", QuantityMerger.merge(listOf("½ cup", "½ cup")))
    }

    @Test
    fun `scales grams up to kilograms`() {
        assertEquals("1.5 kg", QuantityMerger.merge(listOf("500 g", "1 kg")))
        assertEquals("1.4 kg", QuantityMerger.merge(listOf("1400 g")))
        // Metric totals stay decimal — "1 1/2 kg" would not read like a recipe.
        assertEquals("1.5 l", QuantityMerger.merge(listOf("500 ml", "1 l")))
    }

    @Test
    fun `sums bare counts without inventing a unit`() {
        assertEquals("5", QuantityMerger.merge(listOf("2", "3")))
    }

    @Test
    fun `keeps units that cannot be combined side by side`() {
        assertEquals("2 cups + 100 g", QuantityMerger.merge(listOf("2 cups", "100 g")))
        // Spoons are a nested family; converting them would produce thirds.
        assertEquals("1 tbsp + 1 tsp", QuantityMerger.merge(listOf("1 tbsp", "1 tsp")))
    }

    @Test
    fun `passes unparseable amounts through untouched`() {
        assertEquals("a pinch", QuantityMerger.merge(listOf("a pinch")))
        assertEquals("200 g + to taste", QuantityMerger.merge(listOf("200 g", "to taste")))
    }

    @Test
    fun `does not read a range as its lower bound`() {
        assertEquals("1-2 tbsp", QuantityMerger.merge(listOf("1-2 tbsp")))
        assertEquals("2 x 400 g can", QuantityMerger.merge(listOf("2 x 400 g can")))
    }

    @Test
    fun `keeps an unknown unit verbatim`() {
        assertEquals("3 large onions", QuantityMerger.merge(listOf("3 large onions")))
        assertEquals("2 glass", QuantityMerger.merge(listOf("1 glass", "1 glass")))
    }

    @Test
    fun `pluralises only above one`() {
        assertEquals("1 cup", QuantityMerger.merge(listOf("1 cup")))
        assertEquals("5 cloves", QuantityMerger.merge(listOf("2 cloves", "3 cloves")))
        assertEquals("2 pinches", QuantityMerger.merge(listOf("1 pinch", "1 pinch")))
    }

    @Test
    fun `ignores blank entries and returns null when nothing is left`() {
        assertNull(QuantityMerger.merge(emptyList()))
        assertNull(QuantityMerger.merge(listOf("", "   ")))
        assertEquals("100 g", QuantityMerger.merge(listOf("100 g", "  ")))
    }
}
