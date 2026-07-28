package com.cook.easypan.easypan.presentation.ingredients_receipt.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cook.easypan.R
import com.cook.easypan.ui.theme.EasyPanTheme

data class ReceiptItem(
    val name: String,
    val quantity: String? = null,
)

data class ReceiptCategory(
    val name: String,
    val items: List<ReceiptItem>,
)

/**
 * The torn-paper groceries receipt from Figma 220:909: EasyPan header, plan meta line, dashed
 * dividers, category sections (item · dotted leader · quantity), a Total row, and a footer.
 * Purely presentational — it renders whatever [categories] it is given.
 */
@Composable
fun GroceriesReceipt(
    meta: String,
    categories: List<ReceiptCategory>,
    itemCount: String,
    modifier: Modifier = Modifier,
) {
    val toothHeight = 8.dp
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(ReceiptShape(toothHeight = toothHeight))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 20.dp)
            .padding(top = toothHeight + 20.dp, bottom = toothHeight + 20.dp),
    ) {
        // Header — logo mark + brand name.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(31.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Restaurant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = meta,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))
        DashedDivider()

        categories.forEach { category ->
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = category.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            category.items.forEach { item ->
                ReceiptItemRow(item = item)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        DashedDivider()
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.ingredients_list_receipt_total),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = itemCount,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.ingredients_list_receipt_footer),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ReceiptItemRow(
    item: ReceiptItem,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.name,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
        // The quantity column (and its dotted leader) only renders when a quantity exists.
        if (item.quantity != null) {
            DottedLeader(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp),
            )
            Text(
                text = item.quantity,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun DashedDivider(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.outlineVariant
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp),
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = size.height,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
        )
    }
}

@Composable
private fun DottedLeader(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.outlineVariant
    Canvas(
        modifier = modifier.height(2.dp),
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = size.height,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(1f, 6f)),
        )
    }
}

@Preview
@Composable
private fun GroceriesReceiptPreview() {
    EasyPanTheme {
        GroceriesReceipt(
            meta = "Weekly Plan · 21 meals · 2 people",
            itemCount = "11 Items",
            categories = listOf(
                ReceiptCategory(
                    name = "Produce",
                    items = listOf(
                        ReceiptItem("Spinach"),
                        ReceiptItem("Tomatoes"),
                        ReceiptItem("Garlic"),
                        ReceiptItem("Bell Peppers"),
                    ),
                ),
                ReceiptCategory(
                    name = "Meat & Fish",
                    items = listOf(
                        ReceiptItem("Chicken Breast"),
                        ReceiptItem("Salmon Fillets"),
                    ),
                ),
                ReceiptCategory(
                    name = "Pantry",
                    items = listOf(
                        ReceiptItem("Rice"),
                        ReceiptItem("Pasta"),
                        ReceiptItem("Olive Oil"),
                    ),
                ),
            ),
            modifier = Modifier.padding(24.dp),
        )
    }
}
