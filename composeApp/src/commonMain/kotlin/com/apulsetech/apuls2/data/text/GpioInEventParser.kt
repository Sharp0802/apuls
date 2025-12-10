package com.apulsetech.apuls2.data.text

import com.apulsetech.apuls2.data.GpioInEvent
import java.text.ParseException

class GpioInEventParser : Parser<GpioInEvent> {
    override fun parse(text: String): GpioInEvent {
        val del0 = text.indexOf(' ')
        if (del0 == -1) throw ParseException("Malformed GPIO in-event", 0)

        val del1 = text.indexOf(' ', del0 + 1)
        if (del1 == -1) throw ParseException("Malformed GPIO in-event", del0 + 1)

        val enabled = when (text.slice(0 until del0)) {
            "0" -> false
            "1" -> true
            else -> throw ParseException("Malformed enabled field in GPIO in-event", 0)
        }

        val time = text.slice((del0 + 1) until del1).toUIntOrNull()
            ?: throw ParseException("Malformed time field in GPIO in-event", del0 + 1)

        val command = text.substring(del1 + 1)

        return GpioInEvent(enabled, time, command)
    }
}