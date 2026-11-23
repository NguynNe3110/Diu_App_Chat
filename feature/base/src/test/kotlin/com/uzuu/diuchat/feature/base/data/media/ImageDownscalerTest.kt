package com.uzuu.diuchat.feature.base.data.media

import android.media.ExifInterface
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ImageDownscalerTest {
    @Test
    fun `calculateInSampleSize computes power-of-two subsampling for large images`() {
        ImageDownscaleMath.calculateInSampleSize(4000, 3000, 1280) shouldBeEqualTo 2
        ImageDownscaleMath.calculateInSampleSize(8000, 6000, 1280) shouldBeEqualTo 4
        ImageDownscaleMath.calculateInSampleSize(3000, 4000, 1280) shouldBeEqualTo 2
        ImageDownscaleMath.calculateInSampleSize(1280, 960, 1280) shouldBeEqualTo 1
        ImageDownscaleMath.calculateInSampleSize(800, 600, 1280) shouldBeEqualTo 1
    }

    @Test
    fun `calculateInSampleSize returns 1 for invalid dimensions`() {
        ImageDownscaleMath.calculateInSampleSize(0, 1000, 1280) shouldBeEqualTo 1
        ImageDownscaleMath.calculateInSampleSize(1000, -5, 1280) shouldBeEqualTo 1
        ImageDownscaleMath.calculateInSampleSize(4000, 3000, 0) shouldBeEqualTo 1
    }

    @Test
    fun `calculateTargetDimensions preserves aspect ratio and never upscales`() {
        ImageDownscaleMath.calculateTargetDimensions(4000, 3000, 1280) shouldBeEqualTo (1280 to 960)
        ImageDownscaleMath.calculateTargetDimensions(3000, 4000, 1280) shouldBeEqualTo (960 to 1280)
        ImageDownscaleMath.calculateTargetDimensions(800, 600, 1280) shouldBeEqualTo (800 to 600)
        ImageDownscaleMath.calculateTargetDimensions(1280, 720, 1280) shouldBeEqualTo (1280 to 720)
    }

    @Test
    fun `computeExifRotationDegrees maps standard EXIF orientations`() {
        ImageDownscaleMath.computeExifRotationDegrees(ExifInterface.ORIENTATION_NORMAL) shouldBeEqualTo 0f
        ImageDownscaleMath.computeExifRotationDegrees(ExifInterface.ORIENTATION_ROTATE_90) shouldBeEqualTo 90f
        ImageDownscaleMath.computeExifRotationDegrees(ExifInterface.ORIENTATION_ROTATE_180) shouldBeEqualTo 180f
        ImageDownscaleMath.computeExifRotationDegrees(ExifInterface.ORIENTATION_ROTATE_270) shouldBeEqualTo 270f
    }
}
