package me.sbogolepov.wvm.exec

import me.sbogolepov.wvm.parser.generator.Value
import me.sbogolepov.wvm.raw.Instruction

inline class Address(val index: UInt)

sealed class Function

class HostFunction : Function()
class UsualFunction(val code: Sequence<Instruction>) : Function()

interface Stack {
    fun <T: Value<*>> push(value: T)

    fun <T: Value<*>> pop(): T

    fun <T: Value<*>> peek(): T
}

// TODO: Add grow, etc
interface Memory {
    fun load(address: Address, count: Int): ByteArray
    fun store(address: Address, value: ByteArray)
}

interface Logger {
    enum class Severity {
        INFO, WARNING, ERROR
    }

    fun log(severity: Severity, message: String)

    fun logState(severity: Severity, machine: Machine)
}

interface Environment {
    fun provide(functionSymbol: String): Function?
}

interface Machine {
    val memory: Memory
    val stack: Stack
    val environment: Environment

    fun exec(instructions: Sequence<Instruction>, locals: ArrayList<Value<*>>)
}