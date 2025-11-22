package com.apulsetech.apuls.data

import java.text.ParseException

enum class TcpMode {
    Server,
    Client
}

class TcpModeParser : IParser<TcpMode> {
    override fun parse(text: String): TcpMode = when (text) {
        "0" -> TcpMode.Server
        "1" -> TcpMode.Client
        else -> throw ParseException("Argument out of range (0..1 expected, got '${text}')", 0)
    }
}
