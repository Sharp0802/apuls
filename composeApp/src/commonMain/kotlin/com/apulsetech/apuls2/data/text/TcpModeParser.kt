package com.apulsetech.apuls2.data.text

import com.apulsetech.apuls2.data.TcpMode
import java.text.ParseException

class TcpModeParser : Parser<TcpMode> {
    override fun parse(text: String): TcpMode = when (text) {
        "0" -> TcpMode.Server
        "1" -> TcpMode.Client
        else -> throw ParseException("Argument out of range (0..1 expected, got '${text}')", 0)
    }
}