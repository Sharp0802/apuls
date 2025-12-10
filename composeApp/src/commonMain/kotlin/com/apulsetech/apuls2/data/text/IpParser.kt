package com.apulsetech.apuls2.data.text

import com.apulsetech.apuls2.data.Ip
import java.text.ParseException

class IpParser : Parser<Ip> {
    override fun parse(text: String): Ip {
        var b0: UByte? = null
        var b1: UByte? = null
        var b2: UByte? = null
        var b3: UByte

        var begin = 0
        for (i in 0..2) {
            val end = text.indexOf('.', begin)
            if (end == -1) throw ParseException("Missing delimiter of IPv4", begin)

            val seg = text.slice(begin until end).toUByteOrNull(10)
                ?: throw ParseException("Invalid segment of IPv4", begin)

            when (i) {
                0 -> b0 = seg
                1 -> b1 = seg
                2 -> b2 = seg
            }

            begin = end + 1
        }

        b3 = text.substring(begin).toUByteOrNull()
            ?: throw ParseException("Invalid segment of IPv4", begin)

        return Ip(b0!!, b1!!, b2!!, b3)
    }
}