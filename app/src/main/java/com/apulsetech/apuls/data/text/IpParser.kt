package com.apulsetech.apuls.data.text

import com.apulsetech.apuls.data.Ip
import java.text.ParseException

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