package com.apulsetech.apuls.data.text

import com.apulsetech.apuls.data.AliveMode
import java.text.ParseException

class AliveModeParser : IParser<AliveMode> {
    override fun parse(text: String): AliveMode = when (text) {
        "0" -> AliveMode.None
        "1" -> AliveMode.Disconnect
        "2" -> AliveMode.Reboot
        else -> throw ParseException("Argument out of range (0..2 expected, got '${text}')", 0)
    }
}