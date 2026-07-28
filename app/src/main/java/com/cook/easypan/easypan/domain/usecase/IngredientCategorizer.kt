package com.cook.easypan.easypan.domain.usecase

import com.cook.easypan.easypan.domain.model.IngredientCategory

/**
 * Assigns a free-text recipe ingredient ("Chicken", "2 cups all-purpose flour") to a grocery
 * [IngredientCategory] by case-insensitive substring matching against a keyword table.
 *
 * Keywords are checked **longest first**, so more specific entries win over overlapping shorter
 * ones ("bell pepper" → PRODUCE beats "pepper" → PANTRY, "eggplant" → PRODUCE beats
 * "egg" → DAIRY_EGGS). Anything unmatched lands in [IngredientCategory.OTHER].
 */
object IngredientCategorizer {

    fun categorize(ingredient: String): IngredientCategory {
        val normalized = ingredient.trim().lowercase()
        if (normalized.isEmpty()) return IngredientCategory.OTHER
        return KEYWORDS_BY_LENGTH.firstOrNull { (keyword, _) -> keyword in normalized }
            ?.second
            ?: IngredientCategory.OTHER
    }

    private val KEYWORD_TABLE: Map<String, IngredientCategory> = buildMap {
        listOf(
            // Produce
            "spinach", "tomato", "garlic", "bell pepper", "eggplant", "onion", "broccoli",
            "avocado", "sweet potato", "potato", "carrot", "salad", "lettuce", "romaine",
            "mushroom", "cucumber", "zucchini", "lemon", "lime", "banana", "apple", "berry",
            "berries", "basil", "parsley", "cilantro", "ginger", "celery", "cabbage", "corn",
            "scallion", "chive", "herb",
        ).forEach { put(it, IngredientCategory.PRODUCE) }

        listOf(
            // Meat & fish
            "chicken", "beef", "pork", "salmon", "fish", "shrimp", "tuna", "turkey", "lamb",
            "bacon", "sausage", "ham", "steak",
        ).forEach { put(it, IngredientCategory.MEAT_FISH) }

        listOf(
            // Dairy & eggs
            "egg", "milk", "butter", "cheese", "yogurt", "cream", "mozzarella", "parmesan",
            "feta",
        ).forEach { put(it, IngredientCategory.DAIRY_EGGS) }

        listOf(
            // Pantry
            "rice", "pasta", "spaghetti", "noodle", "flour", "sugar", "salt", "black pepper",
            "pepper", "oil", "vinegar", "soy sauce", "bean", "chickpea", "lentil", "quinoa",
            "tofu", "bread", "tortilla", "honey", "oat", "baking powder", "baking soda",
            "vanilla", "cinnamon", "cumin", "paprika", "chili", "stock", "broth", "nut",
            "almond", "peanut", "sesame", "syrup", "chocolate", "spice",
        ).forEach { put(it, IngredientCategory.PANTRY) }
    }

    // Longest keyword first so specific entries take precedence over shorter overlaps.
    private val KEYWORDS_BY_LENGTH: List<Pair<String, IngredientCategory>> =
        KEYWORD_TABLE.entries
            .map { it.key to it.value }
            .sortedByDescending { it.first.length }
}
