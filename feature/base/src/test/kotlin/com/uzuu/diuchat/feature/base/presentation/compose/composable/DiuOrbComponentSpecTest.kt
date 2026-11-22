package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.ui.graphics.Color
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DiuOrbComponentSpecTest {
    @Test
    fun `validateOrbSize throws on non-positive size and parseMoodColorHex falls back to MoodThuong`() {
        assertThrows<IllegalArgumentException> { validateOrbSize(0f) }
        assertThrows<IllegalArgumentException> { validateOrbSize(-12f) }
        assertThrows<IllegalArgumentException> { validateOrbSize(Float.NaN) }

        parseMoodColorHex("#F4CA84") shouldBeEqualTo AppPalette.MoodVui
        parseMoodColorHex("#A2D7AC") shouldBeEqualTo AppPalette.MoodBinhYen
        parseMoodColorHex("invalid-hex") shouldBeEqualTo AppPalette.MoodThuong
        parseMoodColorHex(null) shouldBeEqualTo AppPalette.MoodThuong
        parseMoodColorHex("", fallback = Color.Black) shouldBeEqualTo Color.Black
    }

    @Test
    fun `calculateRippleRingProgress staggers 3 rings evenly across 0 to 1 cycle`() {
        calculateRippleRingProgress(basePhase = 0.1f, ringIndex = 0, ringCount = 3) shouldBeEqualTo 0.1f
        val ring1 = calculateRippleRingProgress(basePhase = 0.1f, ringIndex = 1, ringCount = 3)
        val ring2 = calculateRippleRingProgress(basePhase = 0.1f, ringIndex = 2, ringCount = 3)
        (ring1 in 0.43f..0.44f) shouldBeEqualTo true
        (ring2 in 0.76f..0.77f) shouldBeEqualTo true
    }
}
