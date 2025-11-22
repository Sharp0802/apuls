package com.apulsetech.apuls.data

import java.text.ParseException

enum class AliveMode {
    None,
    Disconnect,
    Reboot
}

class AliveModeParser : IParser<AliveMode> {
    override fun parse(text: String): AliveMode = when (text) {
        "0" -> AliveMode.None
        "1" -> AliveMode.Disconnect
        "2" -> AliveMode.Reboot
        else -> throw ParseException("Argument out of range (0..2 expected, got '${text}')", 0)
    }
}
