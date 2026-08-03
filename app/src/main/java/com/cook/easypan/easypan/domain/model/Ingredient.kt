package com.cook.easypan.easypan.domain.model

/**
 * A single recipe ingredient with its amount kept separate from its name,
 * so the name alone can be categorised and deduped for the groceries list.
 *
 * @param name what to buy, e.g. "Milk". Never blank — blank entries are dropped when mapping.
 * @param quantity how much, e.g. "1/2 cup". Empty when the recipe does not specify an amount.
 */
data class Ingredient(
    val name: String,
    val quantity: String = ""
)
