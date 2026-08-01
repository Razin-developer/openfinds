package com.openfinds.app.core.network.protocol

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readFully
import io.ktor.utils.io.readInt
import io.ktor.utils.io.writeFully
import io.ktor.utils.io.writeInt

/**
 * Length-prefixed framing so TCP stream boundaries always match message
 * boundaries: a 4-byte big-endian length header, then that many payload bytes.
 */
object Framing {
    private const val MAX_FRAME_BYTES = 1 shl 20 // 1 MiB is far more than any OpenFind message needs

    suspend fun writeFrame(
        output: ByteWriteChannel,
        payload: ByteArray,
    ) {
        output.writeInt(payload.size)
        output.writeFully(payload)
        output.flush()
    }

    suspend fun readFrame(input: ByteReadChannel): ByteArray {
        val length = input.readInt()
        require(length in 0..MAX_FRAME_BYTES) { "Refusing to read frame of size $length" }
        val buffer = ByteArray(length)
        input.readFully(buffer)
        return buffer
    }
}
