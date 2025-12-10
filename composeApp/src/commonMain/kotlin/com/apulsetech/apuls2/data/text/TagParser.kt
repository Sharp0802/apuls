package com.apulsetech.apuls2.data.text

import com.apulsetech.apuls2.data.Ip
import com.apulsetech.apuls2.data.Tag

class TagParser : Parser<Tag> {
    override fun parse(text: String): Tag {
        val fields = text.split(' ')

        val tag: String = fields[0]

        var ant: Int? = null
        var rssi: Int? = null
        var rid: Int? = null
        var freq: Int? = null
        var ip: Ip? = null
        var date: String? = null
        var cs: Int? = null

        for (field in fields.slice(1 until fields.size)) {
            val i = field.indexOf('=')
            val name = field.slice(0 until i)
            val value = field.substring(i + 1)

            when (name) {
                "ant" -> ant = value.toInt()
                "rssi" -> rssi = value.toInt()
                "rid" -> rid = value.toInt()
                "freq" -> freq = value.toInt()
                "ip" -> ip = value.parse()
                "date" -> date = value
                "cs" -> cs = value.toInt(16)
                else -> {
                    println("Unrecognized field '$name'")
                }
            }
        }

        return Tag(tag, ant, rssi, rid, freq, ip, date, cs)
    }
}