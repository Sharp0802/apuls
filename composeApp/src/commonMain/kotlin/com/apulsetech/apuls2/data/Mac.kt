package com.apulsetech.apuls2.data

import java.io.Serializable

data class Mac(
    val b0: UByte,
    val b1: UByte,
    val b2: UByte,
    val b3: UByte,
    val b4: UByte,
    val b5: UByte
) : Serializable {
    private fun nibble(n: Int): Char = "0123456789abcdef"[n]

    override fun toString(): String {
        val buffer = CharArray(17)

        fun write(pos: Int, value: UByte) {
            val v = value.toInt() and 0xFF
            buffer[pos + 0] = nibble(v ushr 4)
            buffer[pos + 1] = nibble(v and 0xF)
        }

        write(0, b0); buffer[2] = ':'
        write(3, b1); buffer[5] = ':'
        write(6, b2); buffer[8] = ':'
        write(9, b3); buffer[11] = ':'
        write(12, b4); buffer[14] = ':'
        write(15, b5)

        return String(buffer)
    }

    operator fun get(index: Int): UByte {
        return when (index) {
            0 -> b0
            1 -> b1
            2 -> b2
            3 -> b3
            4 -> b4
            5 -> b5
            else -> throw ArrayIndexOutOfBoundsException(index)
        }
    }
}
