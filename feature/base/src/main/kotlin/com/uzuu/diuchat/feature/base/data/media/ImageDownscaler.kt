package com.uzuu.diuchat.feature.base.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

data class ImageDownscaleConfig(
    val maxDimensionPx: Int = DEFAULT_MAX_DIMENSION_PX,
    val jpegQuality: Int = DEFAULT_JPEG_QUALITY,
    val maxFileSizeBytes: Int = DEFAULT_MAX_FILE_SIZE_BYTES,
    val minJpegQuality: Int = DEFAULT_MIN_JPEG_QUALITY,
) {
    companion object {
        const val DEFAULT_MAX_DIMENSION_PX = 1280
        const val DEFAULT_JPEG_QUALITY = 80
        const val DEFAULT_MAX_FILE_SIZE_BYTES = 500 * 1024
        const val DEFAULT_MIN_JPEG_QUALITY = 55
        const val QUALITY_STEP = 10
    }
}

interface ImageDownscaler {
    suspend fun downscale(
        uriString: String,
        config: ImageDownscaleConfig = ImageDownscaleConfig(),
    ): ByteArray?
}

object ImageDownscaleMath {
    private const val ROTATION_90 = 90f
    private const val ROTATION_180 = 180f
    private const val ROTATION_270 = 270f

    /**
     * Tính `BitmapFactory.Options.inSampleSize` là lũy thừa của 2 để giảm bộ nhớ RAM
     * ngay trong bước decode native mà vẫn giữ kích thước >= [maxDimensionPx] cho bước scale mịn.
     */
    fun calculateInSampleSize(
        srcWidth: Int,
        srcHeight: Int,
        maxDimensionPx: Int,
    ): Int {
        if (srcWidth <= 0 || srcHeight <= 0 || maxDimensionPx <= 0) return 1
        var inSampleSize = 1
        val halfWidth = srcWidth / 2
        val halfHeight = srcHeight / 2
        while (max(halfWidth / inSampleSize, halfHeight / inSampleSize) >= maxDimensionPx) {
            inSampleSize *= 2
        }
        return inSampleSize
    }

    /**
     * Tính kích thước đích giữ nguyên tỷ lệ khung hình, không phóng to ảnh nhỏ hơn [maxDimensionPx].
     */
    fun calculateTargetDimensions(
        srcWidth: Int,
        srcHeight: Int,
        maxDimensionPx: Int,
    ): Pair<Int, Int> {
        if (srcWidth <= 0 || srcHeight <= 0 || maxDimensionPx <= 0) {
            return srcWidth.coerceAtLeast(1) to srcHeight.coerceAtLeast(1)
        }
        val longestSide = max(srcWidth, srcHeight)
        if (longestSide <= maxDimensionPx) {
            return srcWidth to srcHeight
        }
        val scale = maxDimensionPx.toFloat() / longestSide.toFloat()
        val targetWidth = (srcWidth * scale).roundToInt().coerceAtLeast(1)
        val targetHeight = (srcHeight * scale).roundToInt().coerceAtLeast(1)
        return targetWidth to targetHeight
    }

    fun computeExifRotationDegrees(orientation: Int): Float = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> ROTATION_90
        ExifInterface.ORIENTATION_ROTATE_180 -> ROTATION_180
        ExifInterface.ORIENTATION_ROTATE_270 -> ROTATION_270
        else -> 0f
    }
}

class AndroidImageDownscaler(
    private val context: Context,
) : ImageDownscaler {
    override suspend fun downscale(
        uriString: String,
        config: ImageDownscaleConfig,
    ): ByteArray? = withContext(Dispatchers.IO) {
        runCatching {
            downscaleUri(Uri.parse(uriString), config)
        }.getOrNull()
    }

    private fun downscaleUri(
        uri: Uri,
        config: ImageDownscaleConfig,
    ): ByteArray? {
        val bounds = decodeBounds(uri) ?: return null
        val sampleSize =
            ImageDownscaleMath.calculateInSampleSize(
                srcWidth = bounds.first,
                srcHeight = bounds.second,
                maxDimensionPx = config.maxDimensionPx,
            )
        val sampledBitmap = decodeSampledBitmap(uri, sampleSize) ?: return null
        val rotationDegrees = readExifRotationDegrees(uri)

        val processedBitmap = scaleAndRotate(sampledBitmap, config.maxDimensionPx, rotationDegrees)
        return try {
            compressToJpeg(processedBitmap, config).also { bytes ->
                Timber.d(
                    "ImageDownscaler: %dx%d (sample=%d) -> %dx%d, %d bytes",
                    bounds.first,
                    bounds.second,
                    sampleSize,
                    processedBitmap.width,
                    processedBitmap.height,
                    bytes.size,
                )
            }
        } finally {
            if (processedBitmap !== sampledBitmap && !processedBitmap.isRecycled) {
                processedBitmap.recycle()
            }
            if (!sampledBitmap.isRecycled) {
                sampledBitmap.recycle()
            }
        }
    }

    private fun decodeBounds(uri: Uri): Pair<Int, Int>? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
        return if (options.outWidth > 0 && options.outHeight > 0) {
            options.outWidth to options.outHeight
        } else {
            null
        }
    }

    private fun decodeSampledBitmap(
        uri: Uri,
        sampleSize: Int,
    ): Bitmap? {
        val options =
            BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
    }

    private fun readExifRotationDegrees(uri: Uri): Float = runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val exif = ExifInterface(stream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
            ImageDownscaleMath.computeExifRotationDegrees(orientation)
        } ?: 0f
    }.getOrDefault(0f)

    private fun scaleAndRotate(
        source: Bitmap,
        maxDimensionPx: Int,
        rotationDegrees: Float,
    ): Bitmap {
        val (targetW, targetH) =
            ImageDownscaleMath.calculateTargetDimensions(
                srcWidth = source.width,
                srcHeight = source.height,
                maxDimensionPx = maxDimensionPx,
            )
        val scaled =
            if (targetW != source.width || targetH != source.height) {
                Bitmap.createScaledBitmap(source, targetW, targetH, true)
            } else {
                source
            }
        if (rotationDegrees == 0f) return scaled

        val matrix = Matrix().apply { postRotate(rotationDegrees) }
        val rotated = Bitmap.createBitmap(scaled, 0, 0, scaled.width, scaled.height, matrix, true)
        if (scaled !== source && scaled !== rotated && !scaled.isRecycled) {
            scaled.recycle()
        }
        return rotated
    }

    private fun compressToJpeg(
        bitmap: Bitmap,
        config: ImageDownscaleConfig,
    ): ByteArray {
        val output = ByteArrayOutputStream()
        var quality = config.jpegQuality.coerceIn(config.minJpegQuality, MAX_JPEG_QUALITY)
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)

        while (output.size() > config.maxFileSizeBytes && quality > config.minJpegQuality) {
            output.reset()
            quality = (quality - ImageDownscaleConfig.QUALITY_STEP).coerceAtLeast(config.minJpegQuality)
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
        }
        return output.toByteArray()
    }

    private companion object {
        const val MAX_JPEG_QUALITY = 100
    }
}
