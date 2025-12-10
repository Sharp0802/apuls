package com.apulsetech.apuls2.data.text

import com.apulsetech.apuls2.data.SelectQuery
import java.text.ParseException

class SelectQueryParser : Parser<SelectQuery> {
    @OptIn(ExperimentalUnsignedTypes::class)
    override fun parse(text: String): SelectQuery {
        val buffer = UIntArray(3)

        var begin = 0
        (0..2).forEach { j ->
            val i = text.indexOf(' ', begin).let {
                when (it) {
                    -1 -> text.length
                    else -> it
                }
            }

            // TODO : is mask hexadecimal-encoded?
            buffer[j] = text.slice(begin until i).toUIntOrNull()
                ?: throw ParseException("Malformed select query segment", begin)

            if (i == 0 && buffer[i] > 4u) {
                throw ParseException(
                    "Bank index out of range (0..3 expected, got ${buffer[i]})",
                    begin
                )
            }

            begin = i + 1
        }

        return SelectQuery(buffer[0], buffer[1], buffer[2])
    }
}