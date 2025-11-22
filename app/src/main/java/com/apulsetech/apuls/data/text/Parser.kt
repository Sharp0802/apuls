package com.apulsetech.apuls.data.text

import java.text.ParseException
import kotlin.jvm.Throws
import kotlin.reflect.KClass

interface IParser<T> {
    @Throws(ParseException::class)
    fun parse(text: String): T
}

fun <T> String.parse(with: IParser<T>): T = with.parse(this)

object Parser {
    private val map = mutableMapOf<KClass<*>, IParser<*>>()

    private inline fun <reified T : Any> register(parser: IParser<T>) {
        map[T::class] = parser
    }

    init {
        register(AliveModeParser())
        register(BaudrateParser())
        register(BooleanParser())
        register(IntParser())
        register(StringParser())
        register(GpioInEventParser())
        register(GpioOutEventParser())
        register(IpParser())
        register(LockOpParser())
        register(MacParser())
        register(MqttQoSParser())
        register(ReadOpParser())
        register(SelectQueryParser())
        register(TagReportModeParser())
        register(TcpModeParser())
        register(WriteOpParser())
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> of(type: KClass<T>): IParser<T> {
        return map[type] as? IParser<T> ?: error("No parser registered for ${type.qualifiedName}")
    }
}

inline fun <reified T : Any> String.parse(): T = Parser.of(T::class).parse(this)
