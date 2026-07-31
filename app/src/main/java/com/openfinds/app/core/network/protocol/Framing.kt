package com.openfinds.app.core.network.protocol

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Length-prefixed framing so TCP stream boundaries always match message
 * boundaries: a 4-byte big-endian length header, then that many payload bytes.
 */
object Framing {
    private const val MAX_FRAME_BYTES = 1 shl 20 // 1 MiB is far more than any OpenFind message needs

    fun writeFrame(output: OutputStream, payload: ByteArray) {
        val dataOutput = DataOutputStream(output)
        dataOutput.writeInt(payload.size)
        dataOutput.write(payload)
        dataOutput.flush()
    }

    fun readFrame(input: InputStream): ByteArray {
        val dataInput = DataInputStream(input)
        val length = dataInput.readInt()
        require(length in 0..MAX_FRAME_BYTES) { "Refusing to read frame of size $length" }
        val buffer = ByteArray(length)
        dataInput.readFully(buffer)
        return buffer
    }
}
