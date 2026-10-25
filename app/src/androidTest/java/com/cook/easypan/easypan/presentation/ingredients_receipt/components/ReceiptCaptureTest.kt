package com.cook.easypan.easypan.presentation.ingredients_receipt.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cook.easypan.ui.theme.EasyPanTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

private const val EXPECTED_WIDTH_PX = 1080

/**
 * Regression cover for the `libhwui` SIGSEGV that killed the app on every share, and for the
 * silently cropped image it would have produced once it stopped crashing.
 */
@RunWith(AndroidJUnit4::class)
class ReceiptCaptureTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun capturesTheWholeReceiptWhileASiblingAnimates() {
        val captured = AtomicReference<ImageBitmap?>()
        val done = AtomicReference(false)

        rule.setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                // The real screen keeps an animating spinner next to the capture, which invalidates
                // every frame and used to re-enter the recording mid-readback.
                CircularProgressIndicator()
                ReceiptCapture(
                    meta = "Weekly Plan · 21 meals · 2 people",
                    categories = longReceipt(),
                    itemCount = "60 Items",
                    onCaptured = { bitmap ->
                        captured.set(bitmap)
                        done.set(true)
                    },
                )
            }
        }

        rule.waitUntil(timeoutMillis = 5_000) { done.get() }

        val bitmap = captured.get()
        assertNotNull("capture returned null", bitmap)
        requireNotNull(bitmap)
        assertEquals(EXPECTED_WIDTH_PX, bitmap.width)

        val screenHeight = InstrumentationRegistry.getInstrumentation()
            .targetContext.resources.displayMetrics.heightPixels
        assertTrue(
            "receipt was cropped to ${bitmap.height}px, screen is ${screenHeight}px",
            bitmap.height > screenHeight
        )
    }

    @Test
    fun capturesAShortReceipt() {
        val captured = AtomicReference<ImageBitmap?>()
        val done = AtomicReference(false)

        rule.setContent {
            ReceiptCapture(
                meta = "Weekly Plan · 3 meals · 1 person",
                categories = listOf(
                    ReceiptCategory(
                        name = "Produce",
                        items = listOf(ReceiptItem("Spinach"), ReceiptItem("Tomatoes", "4")),
                    ),
                ),
                itemCount = "2 Items",
                onCaptured = { bitmap ->
                    captured.set(bitmap)
                    done.set(true)
                },
            )
        }

        rule.waitUntil(timeoutMillis = 5_000) { done.get() }

        val bitmap = captured.get()
        assertNotNull("capture returned null", bitmap)
        requireNotNull(bitmap)
        assertEquals(EXPECTED_WIDTH_PX, bitmap.width)
        assertTrue("bitmap has no height", bitmap.height > 0)
    }

    @Test
    fun capturesAlongsideAVisibleReceipt() {
        val captured = AtomicReference<ImageBitmap?>()
        val done = AtomicReference(false)
        val categories = longReceipt()

        rule.setContent {
            EasyPanTheme {
                Scaffold(
                    bottomBar = { CircularProgressIndicator() },
                ) { padding ->
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            GroceriesReceipt(
                                meta = "Weekly Plan · 21 meals · 2 people",
                                categories = categories,
                                itemCount = "60 Items",
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        ReceiptCapture(
                            meta = "Weekly Plan · 21 meals · 2 people",
                            categories = categories,
                            itemCount = "60 Items",
                            onCaptured = { bitmap ->
                                captured.set(bitmap)
                                done.set(true)
                            },
                        )
                    }
                }
            }
        }

        rule.waitUntil(timeoutMillis = 5_000) { done.get() }
        rule.waitForIdle()

        val bitmap = captured.get()
        assertNotNull("capture returned null", bitmap)
        requireNotNull(bitmap)
        assertEquals(EXPECTED_WIDTH_PX, bitmap.width)
    }

    /** Long enough that the receipt cannot fit on any phone screen. */
    private fun longReceipt(): List<ReceiptCategory> = List(6) { categoryIndex ->
        ReceiptCategory(
            name = "Category $categoryIndex",
            items = List(10) { itemIndex ->
                ReceiptItem(
                    name = "Ingredient $categoryIndex-$itemIndex",
                    quantity = "$itemIndex g"
                )
            },
        )
    }
}
