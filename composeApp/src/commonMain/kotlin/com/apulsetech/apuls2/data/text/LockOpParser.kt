package com.apulsetech.apuls2.data.text

import com.apulsetech.apuls2.data.Access
import com.apulsetech.apuls2.data.LockOp
import com.apulsetech.apuls2.data.Mask
import java.text.ParseException

class LockOpParser : Parser<LockOp> {
    override fun parse(text: String): LockOp {
        val delimiter = text.indexOf(' ')
        if (delimiter == -1) {
            throw ParseException("Malformed lock operation", 0)
        }

        val mask = text.slice(0 until delimiter)
        if (mask.length != 5) throw ParseException(
            "Invalid length of mask (5 expected, got ${mask.length})", 0
        )

        val masks = arrayOf(
            Mask.None, Mask.None, Mask.None, Mask.None, Mask.None
        )
        for (i in 0 until 5) {
            masks[i] = when (mask[i]) {
                '0' -> Mask.None
                '1' -> Mask.PermanentOnly
                '2' -> Mask.PasswordRequiredOnly
                '3' -> Mask.All
                else -> throw ParseException("Mask out of range (0..3 expected, got ${mask[i]})", i)
            }
        }

        val access = text.substring(delimiter + 1)
        if (access.length != 5) throw ParseException(
            "Invalid length of action (5 expected, got ${mask.length})", 0
        )

        val accesses = arrayOf(
            Access.Accessible,
            Access.Accessible,
            Access.Accessible,
            Access.Accessible,
            Access.Accessible
        )
        for (i in 0 until 5) {
            accesses[i] = when (access[i]) {
                '0' -> Access.Accessible
                '1' -> Access.AccessiblePermanently
                '2' -> Access.PasswordRequired
                '3' -> Access.NotAccessible
                else -> throw ParseException(
                    "Access out of range (0..3 expected, got ${mask[i]})",
                    i
                )
            }
        }

        return LockOp(
            Pair(masks[0], accesses[0]),
            Pair(masks[1], accesses[1]),
            Pair(masks[2], accesses[2]),
            Pair(masks[3], accesses[3]),
            Pair(masks[4], accesses[4])
        )
    }
}