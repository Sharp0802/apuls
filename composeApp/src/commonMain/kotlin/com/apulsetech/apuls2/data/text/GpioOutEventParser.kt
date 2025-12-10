package com.apulsetech.apuls2.data.text

import com.apulsetech.apuls2.data.GpioOutEvent
import java.text.ParseException

class GpioOutEventParser : Parser<GpioOutEvent> {
    override fun parse(text: String): GpioOutEvent {
        val del0 = text.indexOf(' ')
        if (del0 == -1) throw ParseException("Malformed GPIO in-event", 0)

        val enabled = when (text.slice(0 until del0)) {
            "0" -> false
            "1" -> true
            else -> throw ParseException("Malformed enabled field in GPIO out-event", 0)
        }

        val time = text.substring(del0 + 1).toUIntOrNull()
            ?: throw ParseException("Malformed time field in GPIO out-event", del0 + 1)

        return GpioOutEvent(enabled, time)
    }
}