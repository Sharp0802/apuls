package com.apulsetech.apuls.data.text

import com.apulsetech.apuls.data.TcpMode
import java.text.ParseException

class TcpModeParser : IParser<TcpMode> {
    override fun parse(text: String): TcpMode = when (text) {
        "0" -> TcpMode.Server
        "1" -> TcpMode.Client
        else -> throw ParseException("Argument out of range (0..1 expected, got '${text}')", 0)
    }
}