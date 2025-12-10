package com.apulsetech.apuls2.command

import com.apulsetech.apuls2.data.text.Parser
import java.text.ParseException
import kotlin.reflect.KClass

data class Command(
    val declaration: CommandDeclaration, val state: Any, val klass: KClass<*>
) {
    companion object {
        inline operator fun <reified T : Any> invoke(
            declaration: CommandDeclaration, state: T
        ): Command {
            return Command(declaration, state, T::class)
        }
    }
}

class CommandParser : Parser<Command> {
    override fun parse(text: String): Command {
        if (text.isEmpty()) throw ParseException("Cannot parse empty string", 0)

        var delimiter = when (text[0]) {
            ':' -> text.indexOf(' ')
            '-' -> text.indexOf('=')
            else -> throw ParseException("Unrecognized STX character '${text[0]}'", 0)
        }
        if (delimiter == -1 && (text.startsWith("-stop") || text.startsWith("-start"))) {
            // why???
            delimiter = text.indexOf(' ')
        }

        val name: String
        if (delimiter == -1) {
            name = text.substring(1)

            for (command in CommandDeclarations.commands) {
                if (command.name == name) {
                    return Command<Unit>(command, Unit)
                }
            }
        } else {
            name = text.slice(1 until delimiter)
            val arg = text.substring(delimiter + 1)

            for (command in CommandDeclarations.parameterizedCommands) {
                if (command.name != name) continue

                val parsed = try {
                    command.parse(arg)
                } catch (e: ParseException) {
                    throw ParseException(e.message, e.errorOffset + delimiter + 1)
                }

                return Command(command, parsed, command.type)
            }
        }

        throw ParseException("Unrecognized command name '${name}'", 1)
    }
}
