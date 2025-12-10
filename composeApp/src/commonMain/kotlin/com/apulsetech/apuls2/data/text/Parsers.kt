package com.apulsetech.apuls2.data.text

import com.apulsetech.apuls2.command.CommandParser
import kotlin.reflect.KClass

object Parsers {
    private val map = mutableMapOf<KClass<*>, Parser<*>>()

    private inline fun <reified T : Any> register(parser: Parser<T>) {
        map[T::class] = parser
    }

    init {
        register(AliveModeParser())
        register(BaudrateParser())
        register(BooleanParser())
        register(IntParser())
        register(GpioInEventParser())
        register(GpioOutEventParser())
        register(IpParser())
        register(LockOpParser())
        register(MacParser())
        register(MqttQoSParser())
        register(ReadOpParser())
        register(SelectQueryParser())
        register(StringParser())
        register(TagParser())
        register(TagReportModeParser())
        register(TcpModeParser())
        register(WriteOpParser())

        register(CommandParser())
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> of(type: KClass<T>): Parser<T> {
        return map[type] as? Parser<T> ?: error("No parser registered for ${type.qualifiedName}")
    }
}

inline fun <reified T : Any> String.parse(): T = Parsers.of(T::class).parse(this)
