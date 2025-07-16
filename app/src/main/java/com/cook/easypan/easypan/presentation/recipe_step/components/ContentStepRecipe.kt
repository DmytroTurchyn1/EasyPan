package com.cook.easypan.easypan.presentation.recipe_step.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Displays a recipe step with a title and description in a styled vertical layout.
 *
 * Renders the title and description texts using Material3 typography within a padded container.
 *
 * @param title The title of the recipe step.
 * @param description The description or instructions for the recipe step.
 */
@Composable
fun ContentStepRecipe(
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = description,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}