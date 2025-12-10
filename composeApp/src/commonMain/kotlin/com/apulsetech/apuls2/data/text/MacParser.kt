package com.apulsetech.apuls2.data.text

import com.apulsetech.apuls2.data.Mac
import java.text.ParseException

class MacParser : Parser<Mac> {
    private fun parseSegment(text: String, range: IntRange): UByte =
        text.slice(range).toUByteOrNull(16)
            ?: throw ParseException("Malformed MAC address segment", range.first)

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun parse(text: String): Mac {
        val buffer = UByteArray(6)
        var begin = 0

        for (i in 0..4) {
            val delimiter = text.indexOf(':', begin)
            buffer[i] = parseSegment(text, begin..delimiter)
            begin = delimiter + 1
        }

        buffer[5] = parseSegment(text, begin until text.length)
        return Mac(buffer[0], buffer[1], buffer[2], buffer[3], buffer[4], buffer[5])
    }
}