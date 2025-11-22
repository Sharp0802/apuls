package com.apulsetech.apuls.data

import java.text.ParseException

data class Ip(val b0: UByte, val b1: UByte, val b2: UByte, val b3: UByte) {
    override fun toString(): String {
        return "$b0.$b1.$b2.$b3"
    }

    operator fun get(index: Int): UByte {
        return when (index) {
            0 -> b0
            1 -> b1
            2 -> b2
            3 -> b3
            else -> throw ArrayIndexOutOfBoundsException(index)
        }
    }
}

class IpParser : IParser<Ip> {
    private fun parseSegment(text: String, range: IntRange): UByte =
        text.slice(range).toUByteOrNull(10)
            ?: throw ParseException("Malformed MAC address segment", range.first)

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun parse(text: String): Ip {
        val buffer = UByteArray(4)
        var begin = 0

        for (i in 0..2) {
            val delimiter = text.indexOf('.', begin)
            buffer[i] = parseSegment(text, begin..delimiter)
            begin = delimiter + 1
        }

        buffer[3] = parseSegment(text, begin until text.length)
        return Ip(buffer[0], buffer[1], buffer[2], buffer[3])
    }
}
