package me.sbogolepov.wvm.exec

import me.sbogolepov.wvm.parser.generator.FunctionType
import me.sbogolepov.wvm.parser.generator.Module
import me.sbogolepov.wvm.parser.generator.Value
import me.sbogolepov.wvm.raw.Instruction


interface Stack<T> {
    fun push(value: T)

    fun pop(): T

    fun peek(): T?
}

abstract class StackBase<T> : Stack<T> {
    private val storage = mutableListOf<T>()

    override fun push(value: T) {
        storage += value
    }

    override fun pop(): T {
        val last = storage.last()
        storage.removeAt(storage.lastIndex)
        return last
    }

    override fun peek(): T? =
        if (storage.isEmpty()) null else storage.last()

    fun investigate(fn: (Int, T) -> Unit) = storage.reversed().forEachIndexed(fn)
}

interface Environment {

    val functions: List<HostFunction>

    val tables: List<TableInstance>

    val mems: List<MemoryInstance>

    val globals: List<GlobalInstance>

    val types: List<FunctionType>

    fun eval(callee: HostFunction, args: List<Value<*>>): List<Value<*>>
}

interface Machine {
    val logger: Logger

    val modules: Map<String, ModuleInstance>
    val store: Store

    var module: ModuleInstance

    val environment: Environment

    fun eval(instructions: List<Instruction>): Value<*>?

    fun exec(entryPoint: FunctionAddress)

    fun loadModule(name: String, module: Module)

    fun investigateStack(fn: (Int, Value<*>) -> Unit)

    fun investigateFrameStack(fn: (Int, Frame) -> Unit)

    fun error(message: String): Nothing
}