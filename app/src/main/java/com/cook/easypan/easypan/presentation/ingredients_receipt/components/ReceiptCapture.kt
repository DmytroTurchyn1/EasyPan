package com.cook.easypan.easypan.presentation.ingredients_receipt.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.cook.easypan.ui.theme.EasyPanTheme

private const val TAG = "ReceiptCapture"

/**
 * Layout width of the captured receipt. Combined with [CAPTURE_DENSITY] this fixes the PNG at
 * 1080px wide on every device.
 */
private val CAPTURE_WIDTH = 360.dp
private const val CAPTURE_DENSITY = 3f

/** Frames to wait for the receipt to be laid out and drawn before giving up on the capture. */
private const val MAX_FRAMES_TO_WAIT = 10

/**
 * Renders [GroceriesReceipt] off-screen and hands back a bitmap of it.
 *
 * The receipt is composed for real — it has to be, since only content in the composition tree can be
 * drawn — but it is never shown: [androidx.compose.ui.graphics.layer.GraphicsLayer.record] captures
 * the draw commands and `drawLayer` is deliberately not called, so nothing reaches the screen.
 *
 * Density, width and theme are all pinned rather than inherited, so the shared image looks the same
 * whichever device it was sent from and whatever display settings that user has.
 *
 * [onCaptured] receives null if the receipt never produced a drawable frame or the readback failed.
 */
@Composable
fun ReceiptCapture(
    meta: String,
    categories: List<ReceiptCategory>,
    itemCount: String,
    onCaptured: (ImageBitmap?) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()
    val currentOnCaptured by rememberUpdatedState(onCaptured)

    CompositionLocalProvider(
        // fontScale is pinned too — a user's large-text setting would otherwise overflow the
        // receipt's fixed-width layout in the shared image.
        LocalDensity provides Density(density = CAPTURE_DENSITY, fontScale = 1f)
    ) {
        EasyPanTheme(darkTheme = false) {
            Box(
                modifier = Modifier
                    .requiredWidth(CAPTURE_WIDTH)
                    .wrapContentHeight(unbounded = true)
                    .drawWithContent {
                        // Recorded but never drawn to the screen.
                        graphicsLayer.record { this@drawWithContent.drawContent() }
                    }
                    // The torn ReceiptShape leaves the corners transparent, and some chat apps
                    // composite transparency onto black — so the backdrop has to be inside the
                    // recorded node, not behind it.
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            ) {
                GroceriesReceipt(
                    meta = meta,
                    categories = categories,
                    itemCount = itemCount,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        var framesWaited = 0
        while (graphicsLayer.size == IntSize.Zero && framesWaited < MAX_FRAMES_TO_WAIT) {
            withFrameNanos { }
            framesWaited++
        }
        if (graphicsLayer.size == IntSize.Zero) {
            Log.e(TAG, "Receipt never drew a frame to capture")
            currentOnCaptured(null)
            return@LaunchedEffect
        }
        val bitmap = runCatching { graphicsLayer.toImageBitmap() }
            .onFailure { error ->
                // Very long receipts can exceed the maximum GPU texture size.
                Log.e(TAG, "Failed to read back the receipt bitmap", error)
            }
            .getOrNull()
        currentOnCaptured(bitmap)
    }
}
