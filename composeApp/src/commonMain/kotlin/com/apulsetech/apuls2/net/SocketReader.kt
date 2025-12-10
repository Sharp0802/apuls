package com.apulsetech.apuls2.net

import com.apulsetech.apuls2.platform.Socket
import java.nio.charset.Charset
import kotlin.math.min

class SocketReader(private val socket: Socket) {
    companion object {
        private const val SIZE = 0x8000
        private const val MASK = SIZE - 1
    }

    private val inner = ByteArray(SIZE)

    private var head: Int = 0
    private var tail: Int = 0

    val size: Int
        get() = head - tail

    val isFull: Boolean
        get() = size >= SIZE

    private operator fun get(i: Int): Byte {
        if (i !in 0 until size) {
            throw IndexOutOfBoundsException("Index $i out of bounds for size $size")
        }

        return inner[(tail + i) and MASK]
    }

    fun receive(): Int {
        if (isFull) return 0

        val headI = head and MASK
        val tailI = tail and MASK

        // Calculate available space in the physical array before wrapping
        // If headI >= tailI, we can write up to the end of the array (SIZE - headI)
        // If headI < tailI, we can only write up to tailI (tailI - headI)
        val len = if (headI >= tailI) SIZE - headI else tailI - headI

        val bytesRead = socket.read(inner, headI, len)
        if (bytesRead > 0) {
            head += bytesRead
        }

        return bytesRead
    }

    private fun consume(length: Int, charset: Charset = Charsets.UTF_8): String {
        require(length >= 0) { "Length must be non-negative" }
        if (length == 0) return ""
        if (length > size) {
            throw IllegalArgumentException("Not enough data: requested=$length, available=$size")
        }

        val tailI = tail and MASK
        val out = ByteArray(length)

        val firstChunk = min(length, SIZE - tailI)

        inner.copyInto(out, 0, tailI, tailI + firstChunk)

        val remaining = length - firstChunk
        if (remaining > 0) {
            inner.copyInto(out, firstChunk, 0, remaining)
        }

        tail += length

        return String(out, charset)
    }

    fun tryReadLine(): String? {
        if (size < 2) return null

        // CR and LF bytes
        val cr = '\r'.code.toByte()
        val lf = '\n'.code.toByte()

        for (i in 0 until size - 1) {
            if (this[i] == cr && this[i + 1] == lf) {
                val totalLength = i + 2
                val fullString = consume(totalLength)
                return fullString.dropLast(2)
            }
        }

        return null
    }
}
