package com.apulsetech.apuls.data

import java.text.ParseException

data class SelectQuery(
    val bank: UInt,
    val bitPtr: UInt,
    val mask: UInt
)

class SelectQueryParser : IParser<SelectQuery> {
    @OptIn(ExperimentalUnsignedTypes::class)
    override fun parse(text: String): SelectQuery {
        val buffer = UIntArray(3)

        var begin = 0
        (0..2).forEach { _ ->
            val i = text.indexOf(' ', begin).let {
                when (it) {
                    -1 -> text.length
                    else -> it
                }
            }

            // TODO : is mask hexadecimal-encoded?
            buffer[i] = text.slice(begin until i).toUIntOrNull()
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
