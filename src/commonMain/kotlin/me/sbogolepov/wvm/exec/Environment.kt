package me.sbogolepov.wvm.exec

import me.sbogolepov.wvm.parser.generator.FunctionType
import me.sbogolepov.wvm.parser.generator.Value

class SimpleEnvironment : Environment {
    override val functions: List<HostFunction> = listOf(
        HostFunction("print", 1),
        HostFunction("number", 0)
    )

    override val tables: List<TableInstance> = listOf(
        TableInstance(emptyList())
    )

    override val mems: List<MemoryInstance> = listOf(
        MemoryInstance(ByteArray(65536))
    )

    override val globals: List<GlobalInstance> = listOf()

    override val types: List<FunctionType> = listOf()

    override fun eval(callee: HostFunction, args: List<Value<*>>): List<Value<*>> = when (callee.name) {
        "print" -> {
            println(args[0])
            emptyList()
        }
        "number" -> {
            listOf(Value.I32(15))
        }
        else -> error("Unknown host function ${callee.name}")
    }
}

class EnvironmentModule(environment: Environment, store: Store) : ModuleInstance {

    override val types: List<FunctionType> = environment.types

    override val funcAddrs: List<FunctionAddress> = environment.functions.mapIndexed { index, function ->
        store.funcs += function
        FunctionAddress(index)
    }

    override val tableAddrs: List<TableAddress> = environment.tables.mapIndexed { index, table ->
        store.tables += table
        TableAddress(index)
    }

    override val memAddrs: List<MemoryAddress> = environment.mems.mapIndexed { index, mem ->
        store.mems += mem
        MemoryAddress(index)
    }

    override val globalAddrs: List<GlobalAddress> = environment.globals.mapIndexed { index, global ->
        store.globals += global
        GlobalAddress(index)
    }

    override val exports = listOf(
        ExportInstance("print", FunctionAddress(0)),
        ExportInstance("number", FunctionAddress(1)),
        ExportInstance("__linear_memory", MemoryAddress(0)),
        ExportInstance("__indirect_function_table", TableAddress(0))
    )
}