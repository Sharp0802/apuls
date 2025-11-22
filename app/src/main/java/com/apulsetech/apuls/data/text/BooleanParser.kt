package com.apulsetech.apuls.data.text

import java.text.ParseException

class BooleanParser : IParser<Boolean> {
    override fun parse(text: String): Boolean {
        return when (text) {
            "0" -> false
            "1" -> true
            else -> throw ParseException("Invalid boolean format '$text'", 0)
        }
    }
}
