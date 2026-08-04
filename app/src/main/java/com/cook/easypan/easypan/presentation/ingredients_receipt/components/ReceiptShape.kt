package com.cook.easypan.easypan.presentation.ingredients_receipt.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp


data class ReceiptShape(
    private val toothWidth: Dp = 14.dp,
    private val toothHeight: Dp = 8.dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val teeth = with(density) { toothHeight.toPx() }
        val step = with(density) { toothWidth.toPx() }.coerceIn(1f, size.width.coerceAtLeast(1f))
        val count = (size.width / step).toInt().coerceAtLeast(1)
        val width = size.width / count // even teeth spanning the full width

        val path = Path().apply {
            // Top edge — zigzag left → right (peaks up at y=0, valleys at y=teeth).
            moveTo(0f, teeth)
            for (i in 0 until count) {
                val startX = i * width
                lineTo(startX + width / 2f, 0f)
                lineTo(startX + width, teeth)
            }
            // Right side down to the bottom zigzag.
            lineTo(size.width, size.height - teeth)
            // Bottom edge — zigzag right → left (peaks down at y=height, valleys at y=height-teeth).
            for (i in 0 until count) {
                val startX = size.width - i * width
                lineTo(startX - width / 2f, size.height)
                lineTo(startX - width, size.height - teeth)
            }
            // Left side back up.
            lineTo(0f, teeth)
            close()
        }
        return Outline.Generic(path)
    }
}
