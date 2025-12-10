package com.apulsetech.apuls2.data.text

import com.apulsetech.apuls2.data.Baudrate
import java.text.ParseException

class BaudrateParser : Parser<Baudrate> {
    override fun parse(text: String): Baudrate {
        return when (text) {
            "9600" -> Baudrate.`9600`
            "19200" -> Baudrate.`19200`
            "28800" -> Baudrate.`28800`
            "38400" -> Baudrate.`38400`
            "57600" -> Baudrate.`57600`
            "76800" -> Baudrate.`76800`
            "115200" -> Baudrate.`115200`
            "230400" -> Baudrate.`230400`
            "460800" -> Baudrate.`460800`
            "576000" -> Baudrate.`576000`
            "921600" -> Baudrate.`921600`
            else -> throw ParseException("Unrecognized baudrate $text", 0)
        }
    }
}