package com.apulsetech.apuls.data

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

class IntParser : IParser<Int> {
    override fun parse(text: String): Int =
        text.toIntOrNull() ?: throw ParseException("Invalid number format '$text'", 0)
}

class StringParser : IParser<String> {
    override fun parse(text: String): String = text
}
