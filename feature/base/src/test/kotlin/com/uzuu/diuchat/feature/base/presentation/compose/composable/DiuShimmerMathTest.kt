package com.uzuu.diuchat.feature.base.presentation.compose.composable

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DiuShimmerMathTest {

    @Test
    fun `returns zero bounds when width or height is non positive`() {
        val zeroWidth = calculateShimmerGradientBounds(widthPx = 0f, heightPx = 100f, progress = 0.5f)
        val negativeHeight = calculateShimmerGradientBounds(widthPx = 200f, heightPx = -10f, progress = 0.5f)

        assertEquals(ShimmerGradientBounds(0f, 0f, 0f, 0f), zeroWidth)
        assertEquals(ShimmerGradientBounds(0f, 0f, 0f, 0f), negativeHeight)
    }

    @Test
    fun `sweeps highlight band monotonically from left offscreen to right offscreen`() {
        val start = calculateShimmerGradientBounds(widthPx = 200f, heightPx = 80f, progress = 0f)
        val mid = calculateShimmerGradientBounds(widthPx = 200f, heightPx = 80f, progress = 0.5f)
        val end = calculateShimmerGradientBounds(widthPx = 200f, heightPx = 80f, progress = 1f)

        assertTrue(start.startX < 0f)
        assertEquals(0f, start.endX, 0.001f)
        assertTrue(mid.startX > start.startX)
        assertTrue(end.startX > mid.startX)
        assertTrue(end.startX >= 200f)
        assertTrue(mid.endY > 0f)
    }

    @Test
    fun `clamps out of range progress values`() {
        val belowZero = calculateShimmerGradientBounds(widthPx = 100f, heightPx = 40f, progress = -0.5f)
        val atZero = calculateShimmerGradientBounds(widthPx = 100f, heightPx = 40f, progress = 0f)
        val aboveOne = calculateShimmerGradientBounds(widthPx = 100f, heightPx = 40f, progress = 1.5f)
        val atOne = calculateShimmerGradientBounds(widthPx = 100f, heightPx = 40f, progress = 1f)

        assertEquals(atZero, belowZero)
        assertEquals(atOne, aboveOne)
    }
}
