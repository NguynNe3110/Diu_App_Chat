package com.uzuu.diuchat.feature.base.presentation.util

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeGreaterThan
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class QrCodeGeneratorTest {
    @Test
    fun `encodeMatrix produces square matrix with both foreground and background modules`() {
        val matrix = QrCodeGenerator.encodeMatrix("https://diu.vn/linh/7K2M9P", sizePx = 128)

        matrix.width shouldBeEqualTo 128
        matrix.height shouldBeEqualTo 128
        matrix.pixels.count { it } shouldBeGreaterThan 0
        matrix.pixels.count { !it } shouldBeGreaterThan 0
    }

    @Test
    fun `encodeMatrix produces deterministic output for identical content`() {
        val first = QrCodeGenerator.encodeMatrix("diu-invite-7K2M9P", sizePx = 96)
        val second = QrCodeGenerator.encodeMatrix("diu-invite-7K2M9P", sizePx = 96)

        first shouldBeEqualTo second
    }

    @Test
    fun `encodeMatrix produces different matrices for different payloads`() {
        val first = QrCodeGenerator.encodeMatrix("https://diu.vn/linh/7K2M9P", sizePx = 96)
        val second = QrCodeGenerator.encodeMatrix("https://diu.vn/an/9X1Z2Q", sizePx = 96)

        first shouldNotBeEqualTo second
    }

    @Test
    fun `encodeMatrix throws when content is blank`() {
        assertThrows<IllegalArgumentException> {
            QrCodeGenerator.encodeMatrix("   ", sizePx = 128)
        }
    }

    @Test
    fun `generateBitmap returns null for blank content or non-positive size`() {
        QrCodeGenerator.generateBitmap("", sizePx = 128).shouldBeNull()
        QrCodeGenerator.generateBitmap("https://diu.vn/qr/test", sizePx = 0).shouldBeNull()
    }
}
