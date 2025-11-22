package com.apulsetech.apuls.data

import java.text.ParseException

enum class TagReportMode {
    Repeat,
    Single
}

class TagReportModeParser : IParser<TagReportMode> {
    override fun parse(text: String): TagReportMode = when (text) {
        "0" -> TagReportMode.Repeat
        "1" -> TagReportMode.Single
        else -> throw ParseException("Argument out of range (0..1 expected, got '${text}')", 0)
    }
}
