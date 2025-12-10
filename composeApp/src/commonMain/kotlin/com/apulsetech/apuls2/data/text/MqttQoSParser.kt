package com.apulsetech.apuls2.data.text

import com.apulsetech.apuls2.data.MqttQoS
import java.text.ParseException

class MqttQoSParser : Parser<MqttQoS> {
    override fun parse(text: String): MqttQoS {
        return when (text) {
            "0" -> MqttQoS.AtMostOnce
            "1" -> MqttQoS.AtLeastOnce
            "2" -> MqttQoS.ExactlyOnce
            else -> throw ParseException("MqttQoS out of range (0..2 expected, got '${text}')", 0)
        }
    }
}