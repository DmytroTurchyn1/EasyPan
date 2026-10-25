package com.cook.easypan.easypan.presentation.ingredients_receipt.components

import android.graphics.Bitmap
import android.graphics.Picture
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.cook.easypan.ui.theme.EasyPanTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

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
 * Ceiling on the captured height. A 1080px-wide ARGB_8888 bitmap costs ~4.3MB per 1000px of height,
 * so an unbounded receipt would OOM rather than share.
 */
private const val MAX_CAPTURE_HEIGHT_PX = 12_000

/** Side of the host's reported size — see [ReceiptCapture] for why it is not zero. */
private const val HOST_SIZE_PX = 1

/**
 * Renders [GroceriesReceipt] off-screen and hands back a bitmap of it.
 *
 * The receipt is composed for real — it has to be, since only content in the composition tree can be
 * drawn — but the host layout reports a 1x1px size and clips, so nothing the user can see reaches
 * the screen. The size is 1px rather than 0 deliberately: a fully collapsed subtree risks being
 * skipped by the draw pass, and the draw pass is what fills the [Picture].
 *
 * Recording goes through [Picture] rather than a `GraphicsLayer`. `Picture.beginRecording` hands
 * back a *software* canvas, so Compose draws nested layers — `GroceriesReceipt` clips itself with
 * [ReceiptShape], which forces one — inline instead of emitting references to RenderNodes owned by
 * the on-screen tree. Recording into a hardware canvas that is never drawn leaves those references
 * dangling and segfaults `libhwui` on readback.
 *
 * The recording runs exactly once. This node is a sibling of the share button's animating spinner,
 * so it is re-drawn every frame, and re-recording while the readback is in flight is a data race.
 *
 * Density, width and theme are all pinned rather than inherited, so the shared image looks the same
 * whichever device it was sent from and whatever display settings that user has. Height is measured
 * unbounded, so the whole list ends up in the image however long it is.
 *
 * [onCaptured] receives null if the receipt never produced a drawable frame, ran past
 * [MAX_CAPTURE_HEIGHT_PX], or the readback failed.
 */
@Composable
fun ReceiptCapture(
    meta: String,
    categories: List<ReceiptCategory>,
    itemCount: String,
    onCaptured: (ImageBitmap?) -> Unit,
) {
    val picture = remember { Picture() }
    val hasRecorded = remember { AtomicBoolean(false) }
    val currentOnCaptured by rememberUpdatedState(onCaptured)

    CompositionLocalProvider(
        // fontScale is pinned too — a user's large-text setting would otherwise overflow the
        // receipt's fixed-width layout in the shared image.
        LocalDensity provides Density(density = CAPTURE_DENSITY, fontScale = 1f)
    ) {
        EasyPanTheme(darkTheme = false) {
            CaptureHost {
                Box(
                    modifier = Modifier
                        .drawWithCache {
                            val width = size.width.toInt()
                            val height = size.height.toInt()
                            onDrawWithContent {
                                if (hasRecorded.compareAndSet(false, true)) {
                                    val recording =
                                        Canvas(picture.beginRecording(width, height))
                                    draw(this, layoutDirection, recording, size) {
                                        this@onDrawWithContent.drawContent()
                                    }
                                    picture.endRecording()
                                }
                                drawIntoCanvas { canvas ->
                                    canvas.nativeCanvas.drawPicture(picture)
                                }
                            }
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
    }

    LaunchedEffect(Unit) {
        var framesWaited = 0
        while (picture.height <= 0 && framesWaited < MAX_FRAMES_TO_WAIT) {
            withFrameNanos { }
            framesWaited++
        }
        currentOnCaptured(picture.toImageBitmapOrNull())
    }
}

/**
 * Measures [content] at the pinned capture width with no height limit, then reports a 1x1px size so
 * the receipt costs nothing in the real layout and is clipped away.
 */
@Composable
private fun CaptureHost(content: @Composable () -> Unit) {
    Layout(
        content = content,
        modifier = Modifier.clipToBounds(),
    ) { measurables, _ ->
        val width = CAPTURE_WIDTH.roundToPx()
        val placeables = measurables.map { measurable ->
            measurable.measure(
                Constraints(
                    minWidth = width,
                    maxWidth = width,
                    maxHeight = Constraints.Infinity,
                )
            )
        }
        layout(HOST_SIZE_PX, HOST_SIZE_PX) {
            placeables.forEach { it.place(0, 0) }
        }
    }
}

private suspend fun Picture.toImageBitmapOrNull(): ImageBitmap? {
    if (width <= 0 || height <= 0) {
        Log.e(TAG, "Receipt never drew a frame to capture")
        return null
    }
    if (height > MAX_CAPTURE_HEIGHT_PX) {
        Log.e(TAG, "Receipt is ${height}px tall, past the ${MAX_CAPTURE_HEIGHT_PX}px cap")
        return null
    }
    return withContext(Dispatchers.Default) {
        runCatching {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            android.graphics.Canvas(bitmap).drawPicture(this@toImageBitmapOrNull)
            bitmap.asImageBitmap()
        }.onFailure { error ->
            Log.e(TAG, "Failed to read back the receipt bitmap", error)
        }.getOrNull()
    }
}
