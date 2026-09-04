package com.ir0.iptv.app.util

import android.graphics.Bitmap
import coil.size.Size
import coil.transform.Transformation

/** Cheap "blur": shrink to a tiny bitmap so bilinear upscaling reads as a soft
 * blur. A real gaussian/stack blur needs android.graphics.RenderEffect, which
 * only exists on API 31+ - this TV's Android 11 (API 30) doesn't have it, and
 * this has to actually render on that device, not just newer ones. */
class DownsampleBlurTransformation(private val targetWidth: Int = 40) : Transformation {
    override val cacheKey: String = "downsample_blur_$targetWidth"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val ratio = input.height.toFloat() / input.width.toFloat()
        val width = targetWidth.coerceAtLeast(1)
        val height = (width * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(input, width, height, true)
    }
}
