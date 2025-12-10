package com.apulsetech.apuls2.data.text

import com.apulsetech.apuls2.data.TagReportMode
import java.text.ParseException

class TagReportModeParser : Parser<TagReportMode> {
    override fun parse(text: String): TagReportMode = when (text) {
        "0" -> TagReportMode.Repeat
        "1" -> TagReportMode.Single
        else -> throw ParseException("Argument out of range (0..1 expected, got '${text}')", 0)
    }
}