package com.uzuu.diuchat.feature.base.presentation.util

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Pure-Kotlin representation of an encoded QR code matrix so it can be unit-tested on JVM
 * without relying on Android's [Bitmap].
 */
data class QrMatrix(
    val width: Int,
    val height: Int,
    val pixels: BooleanArray,
) {
    operator fun get(
        x: Int,
        y: Int,
    ): Boolean = pixels[y * width + x]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QrMatrix) return false
        return width == other.width && height == other.height && pixels.contentEquals(other.pixels)
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + pixels.contentHashCode()
        return result
    }
}

/**
 * Generates QR code matrices and Android [Bitmap]s locally using ZXing.
 */
object QrCodeGenerator {
    private const val DEFAULT_SIZE_PX = 512
    private const val DEFAULT_MARGIN = 1

    fun encodeMatrix(
        content: String,
        sizePx: Int = DEFAULT_SIZE_PX,
        margin: Int = DEFAULT_MARGIN,
    ): QrMatrix {
        require(content.isNotBlank()) { "QR content must not be blank" }
        require(sizePx > 0) { "QR sizePx must be positive" }
        require(margin >= 0) { "QR margin must be non-negative" }

        val hints =
            mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN to margin,
            )

        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = BooleanArray(width * height)

        for (y in 0 until height) {
            val rowOffset = y * width
            for (x in 0 until width) {
                pixels[rowOffset + x] = bitMatrix[x, y]
            }
        }

        return QrMatrix(width = width, height = height, pixels = pixels)
    }

    fun generateBitmap(
        content: String,
        sizePx: Int = DEFAULT_SIZE_PX,
        foregroundColor: Int = AndroidColor.BLACK,
        backgroundColor: Int = AndroidColor.WHITE,
        margin: Int = DEFAULT_MARGIN,
    ): Bitmap? {
        if (content.isBlank() || sizePx <= 0) return null

        return runCatching {
            val matrix = encodeMatrix(content = content, sizePx = sizePx, margin = margin)
            val argbPixels =
                IntArray(matrix.width * matrix.height) { index ->
                    if (matrix.pixels[index]) foregroundColor else backgroundColor
                }
            Bitmap.createBitmap(
                argbPixels,
                matrix.width,
                matrix.height,
                Bitmap.Config.ARGB_8888,
            )
        }.getOrNull()
    }
}
