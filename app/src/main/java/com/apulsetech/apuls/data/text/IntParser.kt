package com.apulsetech.apuls.data.text

import java.text.ParseException

class IntParser : IParser<Int> {
    override fun parse(text: String): Int =
        text.toIntOrNull() ?: throw ParseException("Invalid number format '$text'", 0)
}
