package com.apulsetech.apuls.data.text

import com.apulsetech.apuls.data.TagReportMode
import java.text.ParseException

class TagReportModeParser : IParser<TagReportMode> {
    override fun parse(text: String): TagReportMode = when (text) {
        "0" -> TagReportMode.Repeat
        "1" -> TagReportMode.Single
        else -> throw ParseException("Argument out of range (0..1 expected, got '${text}')", 0)
    }
}