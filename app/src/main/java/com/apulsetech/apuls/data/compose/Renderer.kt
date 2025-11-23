package com.apulsetech.apuls.data.compose

import kotlin.reflect.KClass

object Renderer {
    private val map = mutableMapOf<KClass<*>, IRenderer<*>>()

    private inline fun <reified T : Any> register(renderer: IRenderer<T>) {
        map[T::class] = renderer
    }

    init {
        register(AccessRenderer())
        register(AliveModeRenderer())
        register(BaudrateRenderer())
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
    fun <T : Any> of(type: KClass<T>): IRenderer<T> {
        return map[type] as? IRenderer<T> ?: error("No renderer registered for ${type.qualifiedName}")
    }
}
