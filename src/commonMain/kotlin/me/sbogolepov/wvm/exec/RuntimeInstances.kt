package me.sbogolepov.wvm.exec

import me.sbogolepov.wvm.parser.generator.*
import me.sbogolepov.wvm.raw.Instruction
import me.sbogolepov.wvm.raw.InstructionSeq

sealed class Address {
    abstract val index: Int
}

class FunctionAddress(override val index: Int) : Address()
class TableAddress(override val index: Int) : Address()
class MemoryAddress(override val index: Int) : Address()
class GlobalAddress(override val index: Int) : Address()

sealed class ExternType {
    class Function(val type: FunctionType) : ExternType()
    class Table(val type: TableType) : ExternType()
    class Memory(val type: MemoryType) : ExternType()
    class Global(val type: GlobalType) : ExternType()
}

sealed class Callable {
    abstract val arity: Int
}

class HostFunction(val name: String, override val arity: Int) : Callable()

class Function(
    val type: FunctionType,
    val module: ModuleInstance,
    val body: FunctionBody
) : Callable() {
    override val arity: Int = type.parameters.size

    val code = (body.code as InstructionSeq).instructions
}

class ExportInstance(val name: String, val value: Address)

interface ModuleInstance {
    val types: List<FunctionType>
    val funcAddrs: List<FunctionAddress>
    val tableAddrs: List<TableAddress>
    val memAddrs: List<MemoryAddress>
    val globalAddrs: List<GlobalAddress>
    val exports: List<ExportInstance>
}

class SimpleModuleInstance(
    override val types: List<FunctionType>,
    override val funcAddrs: List<FunctionAddress>,
    override val tableAddrs: List<TableAddress>,
    override val memAddrs: List<MemoryAddress>,
    override val globalAddrs: List<GlobalAddress>,
    override val exports: List<ExportInstance>
) : ModuleInstance

class TableInstance(
    val elements: List<FunctionAddress?>,
    val max: Int? = null
)

class MemoryInstance(
    val memory: ByteArray,
    val max: Int? = null
) {
    init {
        require(memory.size % 65536 == 0) { "Memory size should be multiple of 65536!" }
    }
}

class GlobalInstance(
    val value: Value<*>,
    val mutable: Boolean
)

interface Store {
    val funcs: MutableList<Callable>
    val tables: MutableList<TableInstance>
    val mems: MutableList<MemoryInstance>
    val globals: MutableList<GlobalInstance>

    fun func(functionAddress: FunctionAddress) = funcs[functionAddress.index]
}

class SimpleStore(
    override val funcs: MutableList<Callable> = mutableListOf(),
    override val tables: MutableList<TableInstance> = mutableListOf(),
    override val mems: MutableList<MemoryInstance> = mutableListOf(),
    override val globals: MutableList<GlobalInstance> = mutableListOf()
) : Store