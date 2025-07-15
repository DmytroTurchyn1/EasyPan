package com.cook.easypan.easypan.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.cook.easypan.R
import com.cook.easypan.easypan.domain.Recipe
import com.cook.easypan.easypan.domain.StepDescription
import com.cook.easypan.easypan.domain.StepType
import com.cook.easypan.ui.theme.EasyPanTheme


@Composable
fun RecipeListItem(
    recipe: Recipe,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                Text(
                    text = "${recipe.cookMinutes} · ${recipe.difficulty} · ${recipe.instructions.size} steps",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
            }
            AsyncImage(
                model = recipe.titleImg,
                contentDescription = stringResource(R.string.dish_image_description),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.auth_img),
                placeholder = painterResource(R.drawable.ic_launcher_background),
                modifier = Modifier
                    .width(200.dp)
                    .height(100.dp)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
        }

    }
}

@Preview(showSystemUi = true)
@Composable
private fun RecipeListItemPreview() {
    EasyPanTheme {
        RecipeListItem(
            recipe = Recipe(
                id = "1",
                title = "Spicy Chicken Stir-Fry",
                cookMinutes = 30,
                difficulty = "Medium",
                instructions = listOf(
                    StepDescription(
                        step = 1,
                        imageUrl = "example",
                        title = "title",
                        description = "some description",
                        stepType = StepType.TEXT,
                        durationSec = 3000
                    )
                ),
                titleImg = "https://www.chilipeppermadness.com/wp-content/uploads/2021/12/Hunan-Chicken-Recipe6.jpg",
                ingredients = listOf("bread"),
                preparationMinutes = 10,
                chips = listOf("Vegetarian", "Gluten-Free"),
            ),
            onClick = {}
        )
    }

}