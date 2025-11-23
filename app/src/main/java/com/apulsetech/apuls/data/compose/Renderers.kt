package com.apulsetech.apuls.data.compose

import kotlin.reflect.KClass

object Renderers {
    private val map = mutableMapOf<KClass<*>, Renderer<*>>()

    private inline fun <reified T : Any> register(renderer: Renderer<T>) {
        map[T::class] = renderer
    }

    init {
        register(AccessRenderer())
        register(AliveModeRenderer())
        register(BaudrateRenderer())
        register(BooleanRenderer())
        register(GpioInEventRenderer())
        register(GpioOutEventRenderer())
        register(IntRenderer())
        register(IpRenderer())
        register(LockOpRenderer())
        register(MaskRenderer())
        register(MqttQoSRenderer())
        register(SelectQueryRenderer())
        register(StringRenderer())
        register(TagReportModeRenderer())
        register(TcpModeRenderer())
        register(WriteOpRenderer())
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> of(type: KClass<T>): Renderer<T> {
        return map[type] as? Renderer<T> ?: error("No renderer registered for ${type.qualifiedName}")
    }
}
