package com.krishifarms.mobile.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

data class CompressResult(
    val outputPath: String,
    val sizeBytes: Long,
    val mimeType: String = "image/jpeg",
)

@Singleton
class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun compress(
        sourcePath: String,
        outputPath: String,
    ): CompressResult = withContext(Dispatchers.IO) {
        val sourceFile = File(sourcePath)
        require(sourceFile.exists()) { "Source image not found: $sourcePath" }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(sourcePath, bounds)

        val sampleSize = calculateSampleSize(
            width = bounds.outWidth,
            height = bounds.outHeight,
            maxDimension = MAX_DIMENSION_PX,
        )

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        var bitmap = BitmapFactory.decodeFile(sourcePath, decodeOptions)
            ?: error("Unable to decode image: $sourcePath")

        bitmap = applyExifOrientation(sourcePath, bitmap)

        val scaled = scaleDownIfNeeded(bitmap, MAX_DIMENSION_PX)
        if (scaled !== bitmap) {
            bitmap.recycle()
            bitmap = scaled
        }

        val outputFile = File(outputPath)
        outputFile.parentFile?.mkdirs()

        var quality = INITIAL_JPEG_QUALITY
        do {
            FileOutputStream(outputFile).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            }
            quality -= QUALITY_STEP
        } while (outputFile.length() > TARGET_MAX_BYTES && quality >= MIN_JPEG_QUALITY)

        bitmap.recycle()

        CompressResult(
            outputPath = outputFile.absolutePath,
            sizeBytes = outputFile.length(),
        )
    }

    private fun calculateSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        var sampleSize = 1
        val largest = max(width, height)
        while (largest / sampleSize > maxDimension * 2) {
            sampleSize *= 2
        }
        return sampleSize
    }

    private fun scaleDownIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val largest = max(width, height)
        if (largest <= maxDimension) return bitmap

        val scale = maxDimension.toFloat() / largest
        val matrix = Matrix().apply { setScale(scale, scale) }
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }

    private fun applyExifOrientation(sourcePath: String, bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(sourcePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    companion object {
        const val MAX_DIMENSION_PX = 1920
        const val INITIAL_JPEG_QUALITY = 80
        const val MIN_JPEG_QUALITY = 50
        const val QUALITY_STEP = 10
        const val TARGET_MAX_BYTES = 1_024_000L
    }
}
