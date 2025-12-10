package com.apulsetech.apuls2.data.text

import java.text.ParseException

class BooleanParser : Parser<Boolean> {
    override fun parse(text: String): Boolean {
        return when (text) {
            "0" -> false
            "1" -> true
            else -> throw ParseException("Invalid boolean format '$text'", 0)
        }
    }
}
