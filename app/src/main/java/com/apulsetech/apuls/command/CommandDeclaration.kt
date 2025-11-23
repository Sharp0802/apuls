package com.apulsetech.apuls.command

import com.apulsetech.apuls.data.text.Parser
import java.text.ParseException
import kotlin.reflect.KClass

open class CommandDeclaration(val name: String, val label: String) {
    companion object {
        inline fun <reified T : Any> parameterized(
            name: String, label: String, constraints: Array<IConstraint>
        ): TypeParameterizedCommandDeclaration<T> =
            TypeParameterizedCommandDeclaration<T>(name, label, constraints)
    }

    fun build(): String = ":$name"
}

open class ParameterizedCommandDeclaration(
    name: String, label: String, val constraints: Array<IConstraint>, val type: KClass<*>
) : CommandDeclaration(name, label) {
    fun parse(value: String): Any {
        val parsed = Parser.of(type).parse(value)

        for (constraint in constraints) {
            if (!constraint.validate(parsed)) {
                throw ParseException("Validation failed", 0)
            }
        }

        return parsed
    }

    fun getter(): String = build() /* just for semantics */

    fun setter(state: Any): String = ":$name $state"
}

class TypeParameterizedCommandDeclaration<T : Any>(
    name: String, label: String, constraints: Array<IConstraint>, type: KClass<T>
) : ParameterizedCommandDeclaration(name, label, constraints, type) {
    companion object {
        inline operator fun <reified T : Any> invoke(
            name: String, label: String, constraints: Array<IConstraint>
        ): TypeParameterizedCommandDeclaration<T> =
            TypeParameterizedCommandDeclaration(name, label, constraints, T::class)
    }

    fun typedSetter(state: T): String = ":$name $state"
}
