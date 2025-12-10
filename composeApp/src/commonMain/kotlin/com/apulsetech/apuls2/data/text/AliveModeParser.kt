package com.apulsetech.apuls2.data.text

import com.apulsetech.apuls2.data.AliveMode
import java.text.ParseException

class AliveModeParser : Parser<AliveMode> {
    override fun parse(text: String): AliveMode = when (text) {
        "0" -> AliveMode.None
        "1" -> AliveMode.Disconnect
        "2" -> AliveMode.Reboot
        else -> throw ParseException("Argument out of range (0..2 expected, got '${text}')", 0)
    }
}