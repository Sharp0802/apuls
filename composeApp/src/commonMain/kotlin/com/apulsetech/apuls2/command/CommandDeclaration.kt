package com.apulsetech.apuls2.command

import com.apulsetech.apuls2.data.text.Parsers
import java.text.ParseException
import kotlin.reflect.KClass

open class CommandDeclaration(val name: String, val label: String) {
    companion object {
        inline fun <reified T : Any> parameterized(
            name: String, label: String, readonly: Boolean, constraints: Array<Constraint>
        ): TypeParameterizedCommandDeclaration<T> =
            TypeParameterizedCommandDeclaration<T>(name, label, constraints, readonly)
    }

    fun build(): String = ":$name"

    fun <T : Any> cast(): TypeParameterizedCommandDeclaration<T> {
        @Suppress("UNCHECKED_CAST")
        return this as TypeParameterizedCommandDeclaration<T>
    }
}

open class ParameterizedCommandDeclaration(
    name: String,
    label: String,
    val constraints: Array<Constraint>,
    val readonly: Boolean,
    val type: KClass<*>
) : CommandDeclaration(name, label) {
    fun parse(value: String): Any {
        val parsed = Parsers.of(type).parse(value)

        for (constraint in constraints) {
            if (!constraint.validate(parsed)) {
                throw ParseException("Validation failed", 0)
            }
        }

        return parsed
    }

    fun getter(): String = build() /* just for semantics */

    fun setter(state: Any): String {
        return if (state is Boolean) {
            ":$name ${if (state) "1" else "0"}"
        } else {
            ":$name $state"
        }
    }
}

class TypeParameterizedCommandDeclaration<T : Any>(
    name: String, label: String, constraints: Array<Constraint>, readonly: Boolean, type: KClass<T>
) : ParameterizedCommandDeclaration(name, label, constraints, readonly, type) {
    companion object {
        inline operator fun <reified T : Any> invoke(
            name: String, label: String, constraints: Array<Constraint>, readonly: Boolean
        ): TypeParameterizedCommandDeclaration<T> =
            TypeParameterizedCommandDeclaration(name, label, constraints, readonly, T::class)
    }

    fun typedSetter(state: T): String = ":$name $state"
}
