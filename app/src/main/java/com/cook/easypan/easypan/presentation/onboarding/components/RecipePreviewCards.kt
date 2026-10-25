package com.cook.easypan.easypan.presentation.onboarding.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cook.easypan.R
import com.cook.easypan.core.presentation.EasyPanText
import com.cook.easypan.core.presentation.rememberAnimationsEnabled
import com.cook.easypan.easypan.presentation.onboarding.RECIPE_CARDS_FADE_IN_MILLIS
import com.cook.easypan.easypan.presentation.onboarding.staggerRunMillis
import com.cook.easypan.easypan.presentation.onboarding.staggeredFraction
import com.cook.easypan.ui.theme.EasyPanTheme

private val CARD_HEIGHT = 91.dp
private val CARD_SPACING = 10.dp
private val IMAGE_WIDTH = 96.dp
private val IMAGE_HEIGHT = 73.dp
private val IMAGE_CORNER = 14.dp
private val CHEVRON_SIZE = 24.dp

private data class PreviewRecipe(
    @param:DrawableRes val image: Int,
    @param:StringRes val title: Int,
    @param:StringRes val meta: Int,
)

private val PREVIEW_RECIPES = listOf(
    PreviewRecipe(
        image = R.drawable.onboarding_recipe_banana_bread,
        title = R.string.onboarding_recipe_1_title,
        meta = R.string.onboarding_recipe_1_meta
    ),
    PreviewRecipe(
        image = R.drawable.onboarding_recipe_apple_charlotte,
        title = R.string.onboarding_recipe_2_title,
        meta = R.string.onboarding_recipe_2_meta
    ),
)

@Composable
fun RecipePreviewCards(
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.onboarding_recipe_cards_description)
    val elapsed = rememberOneShotElapsed(
        durationMillis = staggerRunMillis(PREVIEW_RECIPES.size, RECIPE_CARDS_FADE_IN_MILLIS),
        isActive = isActive,
        enabled = rememberAnimationsEnabled()
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(CARD_SPACING)
    ) {
        PREVIEW_RECIPES.forEachIndexed { index, recipe ->
            RecipePreviewCard(
                recipe = recipe,
                alpha = {
                    LinearOutSlowInEasing.transform(
                        staggeredFraction(
                            elapsedMillis = elapsed.value.toInt(),
                            index = index,
                            fadeInMillis = RECIPE_CARDS_FADE_IN_MILLIS
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun RecipePreviewCard(
    recipe: PreviewRecipe,
    alpha: () -> Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(CARD_HEIGHT)
            // Reading alpha() inside the graphicsLayer block defers it to the draw phase.
            .graphicsLayer { this.alpha = alpha() }
            .onboardingCard(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            .padding(start = 9.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(recipe.image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = IMAGE_WIDTH, height = IMAGE_HEIGHT)
                .clip(RoundedCornerShape(IMAGE_CORNER))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            EasyPanText(
                text = stringResource(recipe.title),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            EasyPanText(
                text = stringResource(recipe.meta),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(CHEVRON_SIZE)
        )
    }
}

@PreviewLightDark
@Composable
private fun RecipePreviewCardsPreview() {
    EasyPanTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(CARD_SPACING)
            ) {
                PREVIEW_RECIPES.forEach { recipe ->
                    RecipePreviewCard(recipe = recipe, alpha = { 1f })
                }
            }
        }
    }
}
