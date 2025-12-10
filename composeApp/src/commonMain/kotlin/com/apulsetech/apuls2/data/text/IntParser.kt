package com.apulsetech.apuls2.data.text

import java.text.ParseException

class IntParser : Parser<Int> {
    override fun parse(text: String): Int =
        text.toIntOrNull() ?: throw ParseException("Invalid number format '$text'", 0)
}
