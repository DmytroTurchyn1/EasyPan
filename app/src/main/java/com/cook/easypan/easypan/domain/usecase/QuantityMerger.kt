package com.cook.easypan.easypan.domain.usecase

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToLong

/**
 * Adds up the free-text amounts recorded for one ingredient across a meal plan, so the groceries
 * list shows `300 g` rather than `100 g + 200 g`.
 *
 * Amounts are authored by hand, so parsing is deliberately conservative: an entry only joins a sum
 * when both its number and its unit are understood. Matching units are summed after plurals and
 * synonyms are normalised (`1 cup` + `2 cups` → `3 cups`), and grams scale to kilograms as
 * millilitres do to litres (`500 g` + `1 kg` → `1.5 kg`).
 *
 * Units from different measurement systems are never converted into one another — `2 cups + 100 g`
 * stays as it is rather than collapsing to a single number that would imply a precision the recipe
 * never had. Anything unparseable (`a pinch`) passes through untouched.
 */
object QuantityMerger {

    /** Returns null when there is nothing to show. */
    fun merge(quantities: List<String>): String? {
        val cleaned = quantities.map { it.trim() }.filter { it.isNotEmpty() }
        if (cleaned.isEmpty()) return null

        // Totals keyed by scaling family, in first-seen order. Unparseable entries keep their
        // original text and are appended after the totals.
        val totals = LinkedHashMap<String, Double>()
        val units = LinkedHashMap<String, String>()
        val passthrough = LinkedHashSet<String>()

        cleaned.forEach { quantity ->
            val measure = parse(quantity)
            if (measure == null) {
                passthrough += quantity
                return@forEach
            }
            val family = FAMILY_BY_UNIT[measure.unit]
            val key = family?.name ?: measure.unit
            val amount = if (family != null) {
                measure.amount * family.factorOf(measure.unit)
            } else {
                measure.amount
            }
            totals[key] = (totals[key] ?: 0.0) + amount
            units.putIfAbsent(key, measure.unit)
        }

        val merged = totals.map { (key, total) ->
            FAMILIES_BY_NAME[key]?.render(total) ?: format(total, units.getValue(key))
        }

        return (merged + passthrough).joinToString(QUANTITY_SEPARATOR)
    }

    private class Measure(val amount: Double, val unit: String)

    /**
     * Splits a leading amount off the unit, so `100g`, `100 g` and `1 1/2 cups` all parse.
     *
     * Returns null when there is no leading number, or when the unit text still contains a digit —
     * `1-2 tbsp` and `2 x 400 g can` would otherwise be silently read as `1 tbsp` and `2 cans`.
     */
    private fun parse(quantity: String): Measure? {
        val text = quantity.lowercase()
        val (amount, amountEnd) = parseAmount(text) ?: return null
        val unitText = text.substring(amountEnd).trim().trimEnd('.').trim()
        if (unitText.any { it.isDigit() }) return null
        return Measure(amount = amount, unit = canonicalUnit(unitText))
    }

    /** The leading amount and the index just past it, trying the richest notation first. */
    private fun parseAmount(text: String): Pair<Double, Int>? {
        MIXED_FRACTION.find(text)?.let { match ->
            val denominator = match.groupValues[3].toDouble()
            if (denominator != 0.0) {
                val amount =
                    match.groupValues[1].toDouble() + match.groupValues[2].toDouble() / denominator
                return Pair(amount, match.range.last + 1)
            }
        }
        FRACTION.find(text)?.let { match ->
            val denominator = match.groupValues[2].toDouble()
            if (denominator != 0.0) {
                return Pair(match.groupValues[1].toDouble() / denominator, match.range.last + 1)
            }
        }
        NUMBER_WITH_UNICODE_FRACTION.find(text)?.let { match ->
            val amount = match.groupValues[1].toDouble() +
                    UNICODE_FRACTIONS.getValue(match.groupValues[2][0])
            return Pair(amount, match.range.last + 1)
        }
        UNICODE_FRACTION.find(text)?.let { match ->
            return Pair(UNICODE_FRACTIONS.getValue(match.groupValues[1][0]), match.range.last + 1)
        }
        DECIMAL.find(text)?.let { match ->
            val amount = match.groupValues[1].replace(',', '.').toDoubleOrNull()
            if (amount != null) return Pair(amount, match.range.last + 1)
        }
        return null
    }

    /**
     * Lowercased unit text to its canonical form, e.g. `grams` → `g`, `tablespoon` → `tbsp`.
     *
     * Normalisation is table-driven rather than a generic "strip the trailing s", which cannot tell
     * a plural from a singular that ends in one (`glass` → `glas`). Units outside the table are kept
     * verbatim, so they still merge with an identically written amount.
     */
    private fun canonicalUnit(unitText: String): String {
        if (unitText.isEmpty()) return ""
        SYNONYMS[unitText]?.let { return it }
        // Multi-word amounts such as "large onions" normalise on the measurement word alone.
        val words = unitText.split(' ')
        val canonicalLast = SYNONYMS[words.last()] ?: return unitText
        return (words.dropLast(1) + canonicalLast).joinToString(" ")
    }

    private fun format(amount: Double, unit: String): String {
        // Fractions read naturally for cooking measures but not for metric weights, where "1.5 kg"
        // beats "1 1/2 kg".
        val number = formatNumber(amount, allowFractions = unit !in METRIC_UNITS)
        if (unit.isEmpty()) return number
        val display = if (amount > 1.0 + FRACTION_EPSILON && unit in PLURALIZABLE_UNITS) {
            pluralize(unit)
        } else {
            unit
        }
        return "$number $display"
    }

    /**
     * Renders common cooking fractions as mixed numbers (`2.5` → `2 1/2`), matching how the source
     * data is written, and otherwise falls back to at most two decimals.
     */
    private fun formatNumber(amount: Double, allowFractions: Boolean): String {
        if (allowFractions) {
            val whole = floor(amount).toLong()
            val remainder = amount - whole
            COMMON_FRACTIONS.firstOrNull { abs(remainder - it.value) < FRACTION_EPSILON }
                ?.let { fraction ->
                    return when {
                        fraction.label.isEmpty() -> whole.toString()
                        whole == 0L -> fraction.label
                        else -> "$whole ${fraction.label}"
                    }
                }
        }
        val rounded = (amount * 100).roundToLong() / 100.0
        return if (rounded == floor(rounded)) {
            rounded.toLong().toString()
        } else {
            rounded.toString().trimEnd('0').trimEnd('.')
        }
    }

    private fun pluralize(unit: String): String =
        if (unit.endsWith("ch") || unit.endsWith("sh") || unit.endsWith("s")) "${unit}es"
        else "${unit}s"

    /**
     * A group of units that convert cleanly into one another, smallest first. Metric only — the
     * factors are exact and the result still reads like something a recipe would say.
     */
    private class Family(
        val name: String,
        private val units: List<Pair<String, Double>>,
    ) {
        /** How many base units one [unit] is worth, e.g. 1 kg = 1000 g. */
        fun factorOf(unit: String): Double = units.first { it.first == unit }.second

        /** Renders in the largest unit that keeps the number at or above 1. */
        fun render(total: Double): String {
            val (unit, factor) = units.lastOrNull { total >= it.second } ?: units.first()
            return format(total / factor, unit)
        }
    }

    private val FAMILIES = listOf(
        Family("mass_metric", listOf("g" to 1.0, "kg" to 1000.0)),
        Family("volume_metric", listOf("ml" to 1.0, "l" to 1000.0)),
    )

    private val FAMILIES_BY_NAME: Map<String, Family> = FAMILIES.associateBy { it.name }

    private val FAMILY_BY_UNIT: Map<String, Family> = mapOf(
        "g" to FAMILIES[0],
        "kg" to FAMILIES[0],
        "ml" to FAMILIES[1],
        "l" to FAMILIES[1],
    )

    private val METRIC_UNITS = setOf("g", "kg", "mg", "ml", "l")

    /** Canonical units stored singular, so they can be pluralised for display. */
    private val PLURALIZABLE_UNITS = setOf(
        "cup", "clove", "slice", "piece", "can", "pinch", "handful", "sprig", "stalk", "head",
        "bunch",
    )

    private val SYNONYMS: Map<String, String> = buildMap {
        listOf("g", "gr", "gram", "grams", "gramme", "grammes").forEach { put(it, "g") }
        listOf("kg", "kilo", "kilos", "kilogram", "kilograms").forEach { put(it, "kg") }
        listOf("mg", "milligram", "milligrams").forEach { put(it, "mg") }
        listOf("ml", "milliliter", "milliliters", "millilitre", "millilitres")
            .forEach { put(it, "ml") }
        listOf("l", "lt", "liter", "liters", "litre", "litres").forEach { put(it, "l") }
        listOf("tsp", "tsps", "teaspoon", "teaspoons").forEach { put(it, "tsp") }
        listOf("tbsp", "tbsps", "tbs", "tablespoon", "tablespoons").forEach { put(it, "tbsp") }
        listOf("oz", "ounce", "ounces").forEach { put(it, "oz") }
        listOf("lb", "lbs", "pound", "pounds").forEach { put(it, "lb") }
        listOf("fl oz", "fluid ounce", "fluid ounces").forEach { put(it, "fl oz") }
        listOf("cup", "cups").forEach { put(it, "cup") }
        listOf("clove", "cloves").forEach { put(it, "clove") }
        listOf("slice", "slices").forEach { put(it, "slice") }
        listOf("piece", "pieces").forEach { put(it, "piece") }
        listOf("can", "cans").forEach { put(it, "can") }
        listOf("pinch", "pinches").forEach { put(it, "pinch") }
        listOf("handful", "handfuls").forEach { put(it, "handful") }
        listOf("sprig", "sprigs").forEach { put(it, "sprig") }
        listOf("stalk", "stalks").forEach { put(it, "stalk") }
        listOf("head", "heads").forEach { put(it, "head") }
        listOf("bunch", "bunches").forEach { put(it, "bunch") }
    }

    private class Fraction(val value: Double, val label: String)

    private val COMMON_FRACTIONS = listOf(
        Fraction(0.0, ""),
        Fraction(1.0 / 8, "1/8"),
        Fraction(1.0 / 4, "1/4"),
        Fraction(1.0 / 3, "1/3"),
        Fraction(1.0 / 2, "1/2"),
        Fraction(2.0 / 3, "2/3"),
        Fraction(3.0 / 4, "3/4"),
    )

    private val UNICODE_FRACTIONS = mapOf(
        '½' to 0.5, '¼' to 0.25, '¾' to 0.75,
        '⅓' to 1.0 / 3, '⅔' to 2.0 / 3, '⅛' to 0.125,
    )

    private val MIXED_FRACTION = Regex("""^(\d+)\s+(\d+)/(\d+)""")
    private val FRACTION = Regex("""^(\d+)/(\d+)""")
    private val NUMBER_WITH_UNICODE_FRACTION = Regex("""^(\d+)\s*([½¼¾⅓⅔⅛])""")
    private val UNICODE_FRACTION = Regex("""^([½¼¾⅓⅔⅛])""")
    private val DECIMAL = Regex("""^(\d+(?:[.,]\d+)?)""")

    private const val FRACTION_EPSILON = 0.02
    private const val QUANTITY_SEPARATOR = " + "
}
