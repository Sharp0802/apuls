package com.apulsetech.apuls.data

import java.text.ParseException

enum class MqttQoS {
    AtMostOnce,
    AtLeastOnce,
    ExactlyOnce
}

class MqttQoSParser : IParser<MqttQoS> {
    override fun parse(text: String): MqttQoS {
        return when (text) {
            "0" -> MqttQoS.AtMostOnce
            "1" -> MqttQoS.AtLeastOnce
            "2" -> MqttQoS.ExactlyOnce
            else -> throw ParseException("MqttQoS out of range (0..2 expected, got '${text}')", 0)
        }
    }
}
