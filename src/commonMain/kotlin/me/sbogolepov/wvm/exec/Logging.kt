package me.sbogolepov.wvm.exec

import me.sbogolepov.wvm.parser.generator.FunctionType
import me.sbogolepov.wvm.parser.generator.Value

interface Logger {
    enum class Severity {
        INFO, WARNING, ERROR
    }

    fun log(severity: Severity, message: String)

    fun info(message: String) = log(severity = Severity.INFO, message = message)

    fun logState(severity: Severity, machine: Machine)
}

class SimpleLogger(val out: (String) -> Unit = ::println) : Logger {
    override fun log(severity: Logger.Severity, message: String) {
        out(message)
    }

    override fun logState(severity: Logger.Severity, machine: Machine) {
        out("Stack:")
        dumpStack(machine) { index, value ->
            out("[$index $value]")
        }
        out("Call stack")
        dumpFrameStack(machine) { index, value ->
            out("[$index $value]")
        }
        out("Store")
        dumpStore(machine.store, out)
    }
}

private fun dumpStore(store: Store, out: (String) -> Unit) {
    out("Functions:")
    store.funcs.forEach { callable ->
        out(
            when (callable) {
                is HostFunction -> "[HOST ${callable.name}]"
                is Function -> "[${callable.type}]"
            }
        )
    }
    out("Memories:")
    store.mems.forEach { mem ->
        out("Memory at ${mem.memory}")
    }
}

private fun dumpStack(machine: Machine, out: (Int, String) -> Unit) {
    machine.investigateStack { idx, value -> out(idx, prettyValue(value)) }
}

private fun dumpFrameStack(machine: Machine, out: (Int, String) -> Unit) {
    machine.investigateFrameStack { idx, frame -> out(idx, prettyFrame(frame)) }
}

private fun prettyFrame(frame: Frame): String {
    val callee = when (frame.function) {
        is Function -> {
            prettyType(frame.function.type)
        }
        is HostFunction -> {
            "HOST ${frame.function.name}"
        }
    }
    return "$callee ${frame.locals.joinToString { prettyValue(it) }}"
}

private fun prettyType(type: FunctionType): String =
    "${type.parameters.joinToString()} -> ${type.results.joinToString()}"

private fun prettyValue(value: Value<*>) = when (value) {
    is Value.I32 -> "i32(${value.value})"
    is Value.I64 -> "i64(${value.value})"
    is Value.F32 -> "f32(${value.value})"
    is Value.F64 -> "f64(${value.value})"
}
