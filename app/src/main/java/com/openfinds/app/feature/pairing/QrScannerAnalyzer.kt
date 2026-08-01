package com.openfinds.app.feature.pairing

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer

/** Decodes QR codes from the live CameraX preview using ZXing against the Y-plane luminance data. */
class QrScannerAnalyzer(private val onDecoded: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val reader = MultiFormatReader()

    override fun analyze(image: ImageProxy) {
        val plane = image.planes.firstOrNull()
        if (plane == null) {
            image.close()
            return
        }
        val rawData = ByteArray(plane.buffer.remaining()).also { plane.buffer.get(it) }
        // CameraX analysis frames are delivered in the sensor's native (landscape) orientation
        // regardless of how the phone is held; imageInfo.rotationDegrees is how much they need
        // to be rotated to appear upright. Without correcting for it, a QR code held upright in
        // front of the (portrait) preview is sideways in the buffer ZXing actually reads, and
        // almost never decodes.
        val (data, width, height) =
            rotateForDisplay(rawData, image.width, image.height, image.imageInfo.rotationDegrees)
        val source = PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false)
        runCatching {
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            reader.decodeWithState(bitmap).text
        }.getOrNull()?.let(onDecoded)
        reader.reset()
        image.close()
    }

    private fun rotateForDisplay(
        data: ByteArray,
        width: Int,
        height: Int,
        rotationDegrees: Int,
    ): Triple<ByteArray, Int, Int> =
        when (rotationDegrees) {
            90 -> Triple(rotate90(data, width, height), height, width)
            180 -> Triple(rotate180(data, width, height), width, height)
            270 -> Triple(rotate270(data, width, height), height, width)
            else -> Triple(data, width, height)
        }

    private fun rotate90(
        data: ByteArray,
        width: Int,
        height: Int,
    ): ByteArray {
        val rotated = ByteArray(data.size)
        var i = 0
        for (x in 0 until width) {
            for (y in height - 1 downTo 0) {
                rotated[i++] = data[y * width + x]
            }
        }
        return rotated
    }

    private fun rotate180(
        data: ByteArray,
        width: Int,
        height: Int,
    ): ByteArray {
        val rotated = ByteArray(data.size)
        val size = width * height
        for (i in 0 until size) {
            rotated[i] = data[size - 1 - i]
        }
        return rotated
    }

    private fun rotate270(
        data: ByteArray,
        width: Int,
        height: Int,
    ): ByteArray {
        val rotated = ByteArray(data.size)
        var i = 0
        for (x in width - 1 downTo 0) {
            for (y in 0 until height) {
                rotated[i++] = data[y * width + x]
            }
        }
        return rotated
    }
}
