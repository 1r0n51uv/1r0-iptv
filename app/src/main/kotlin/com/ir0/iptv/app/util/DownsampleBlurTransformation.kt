package com.ir0.iptv.app.util

import android.graphics.Bitmap
import coil.size.Size
import coil.transform.Transformation

/**
 * Blur per lo sfondo dell'hero. Un vero gaussian/stack blur di sistema serve
 * android.graphics.RenderEffect, che esiste solo da API 31+: questa TV e' Android 11
 * (API 30), quindi il blur va calcolato a mano.
 *
 * La locandina viene ridotta a una larghezza intermedia (non minuscola, cosi' non si vede
 * la scacchiera dei pixel quando Coil la riporta a schermo) e poi passata piu' volte in un
 * box blur separabile: tre passate di box blur approssimano bene una gaussiana, e su un
 * bitmap di ~200 px il costo e' trascurabile.
 */
class DownsampleBlurTransformation(
    private val targetWidth: Int = 360,
    private val radius: Int = 4,
    private val passes: Int = 2
) : Transformation {
    override val cacheKey: String = "downsample_blur_${targetWidth}_${radius}_$passes"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val ratio = input.height.toFloat() / input.width.toFloat()
        val width = targetWidth.coerceAtLeast(1)
        val height = (width * ratio).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(input, width, height, true)

        val pixels = IntArray(width * height)
        scaled.getPixels(pixels, 0, width, 0, 0, width, height)
        val effectiveRadius = radius.coerceIn(1, minOf(width, height) / 2)
        repeat(passes) {
            boxBlur(pixels, width, height, effectiveRadius, horizontal = true)
            boxBlur(pixels, width, height, effectiveRadius, horizontal = false)
        }

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        output.setPixels(pixels, 0, width, 0, 0, width, height)
        if (scaled != input) scaled.recycle()
        return output
    }

    /** Media mobile su una finestra di (2*radius+1) pixel, in orizzontale o in verticale.
     * L'alpha resta invariata: le locandine sono opache e non serve mediarla. */
    private fun boxBlur(pixels: IntArray, width: Int, height: Int, radius: Int, horizontal: Boolean) {
        val lineLength = if (horizontal) width else height
        val lineCount = if (horizontal) height else width
        val window = radius * 2 + 1
        val line = IntArray(lineLength)

        for (l in 0 until lineCount) {
            for (i in 0 until lineLength) {
                line[i] = if (horizontal) pixels[l * width + i] else pixels[i * width + l]
            }

            var sumR = 0
            var sumG = 0
            var sumB = 0
            for (i in -radius..radius) {
                val c = line[i.coerceIn(0, lineLength - 1)]
                sumR += (c shr 16) and 0xFF
                sumG += (c shr 8) and 0xFF
                sumB += c and 0xFF
            }

            val alphaMask = 0xFF000000.toInt()
            for (i in 0 until lineLength) {
                val blended = (line[i] and alphaMask) or
                    ((sumR / window) shl 16) or
                    ((sumG / window) shl 8) or
                    (sumB / window)
                if (horizontal) pixels[l * width + i] = blended else pixels[i * width + l] = blended

                val outgoing = line[(i - radius).coerceIn(0, lineLength - 1)]
                val incoming = line[(i + radius + 1).coerceIn(0, lineLength - 1)]
                sumR += ((incoming shr 16) and 0xFF) - ((outgoing shr 16) and 0xFF)
                sumG += ((incoming shr 8) and 0xFF) - ((outgoing shr 8) and 0xFF)
                sumB += (incoming and 0xFF) - (outgoing and 0xFF)
            }
        }
    }
}
