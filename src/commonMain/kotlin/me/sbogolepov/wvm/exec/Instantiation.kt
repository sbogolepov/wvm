package me.sbogolepov.wvm.exec

import me.sbogolepov.wvm.parser.generator.*
import me.sbogolepov.wvm.raw.ConstantExpression
import me.sbogolepov.wvm.raw.InstructionSeq

private inline fun <reified T : Section> Module.firstSection(): T =
    sections.filterIsInstance<T>().first()

private inline fun <reified T : Section> Module.firstSectionOrNull(): T? =
    sections.filterIsInstance<T>().firstOrNull()

private fun findImport(import: Import, modules: Map<String, ModuleInstance>): Address? =
    modules.filter { it.key == import.module }.toList().firstOrNull()?.second
        ?.exports?.firstOrNull { it.name == import.name }?.value

private fun Machine.initializeGlobal(global: Global): Value<*> = when (global.expression) {
    is InstructionSeq -> eval(global.expression.instructions.toList()) ?: error("")
    is ConstantExpression -> TODO()
    else -> TODO()
}

fun Machine.instantiate(module: Module, entryPoint: String? = null): ModuleInstance {
    val importedAddresses = module.firstSection<ImportSection>().imports.map {
        findImport(it, modules) ?: error("Not found: ${it.name}")
    }
    val globalValues = module.firstSectionOrNull<GlobalSection>()?.globals?.map { initializeGlobal(it) }
        ?: emptyList()

    val instance = allocate(module, importedAddresses, globalValues)

    tryStartExecution(module, instance, entryPoint)

    return instance
}

private fun Machine.tryStartExecution(
    module: Module,
    instance: ModuleInstance,
    entryPoint: String?
) {
    module.sections.firstOrNull { it is StartSection }?.let {
        val entryPointIdx = module.firstSection<StartSection>().start
        this.module = instance
        exec(instance.funcAddrs[entryPointIdx.toInt()])
    } ?: if (entryPoint != null) {
        this.module = instance
        val address = instance.exports.firstOrNull { it.name == entryPoint }
            ?: error("Aborting: Cannot find entry point $entryPoint")
        logger.info("Executing $entryPoint at address ${address.value.index}")
        logger.logState(Logger.Severity.ERROR, this)
        exec(instance.funcAddrs[address.value.index])
    }
}

// TODO: It's possible to make it lazier
private fun Machine.allocateFunction(module: Module, index: Int, instance: ModuleInstance): FunctionAddress {
    val typeIndex = module.firstSection<FunctionSection>().typesIndices[index].toInt()
    val type = module.firstSection<TypeSection>().types[typeIndex]
    val body = module.firstSection<CodeSection>().entries[index].code
    val function = Function(type, instance, body)
    store.funcs += function
    return FunctionAddress(store.funcs.lastIndex)
}

private fun Machine.allocateTable(tableType: TableType): TableAddress {
    val (min, max) = when (val lim = tableType.limits) {
        is Limits.Open -> lim.min.toInt() to null
        is Limits.Closed -> lim.min.toInt() to lim.max
    }
    val init: List<FunctionAddress?> = (0 until min).map { null }
    val table = TableInstance(init, max?.toInt())
    store.tables += table
    return TableAddress(store.tables.lastIndex)
}

private fun Machine.allocateMemory(memoryType: MemoryType): MemoryAddress {
    val (min, max) = when (val lim = memoryType.limits) {
        is Limits.Open -> lim.min.toInt() to null
        is Limits.Closed -> lim.min.toInt() to lim.max.toInt()
    }
    val memory = MemoryInstance(ByteArray(min * 65536), max?.times(65536))
    store.mems += memory
    return MemoryAddress(store.mems.lastIndex)
}

private fun Machine.allocateGlobal(global: Global, value: Value<*>): GlobalAddress {
    val global = GlobalInstance(value, global.type.mutable)
    store.globals += global
    return GlobalAddress(store.globals.lastIndex)
}

private fun Machine.allocate(
    module: Module, importedAddresses: List<Address>, globalValues: List<Value<*>>
): ModuleInstance {

    val funcAddresses = run {
        val size = module.firstSectionOrNull<FunctionSection>()?.typesIndices?.size ?: 0
        val moduleAddrs = (0 until size).map { FunctionAddress(store.funcs.size + it) }
        importedAddresses.filterIsInstance<FunctionAddress>() + moduleAddrs
    }
    val globalAddresses = run {
        val size = module.firstSectionOrNull<GlobalSection>()?.globals?.size ?: 0
        val moduleAddrs = (0 until size).map { GlobalAddress(store.globals.size + it) }
        importedAddresses.filterIsInstance<GlobalAddress>() + moduleAddrs
    }
    val tableAddresses = run {
        val size = module.firstSectionOrNull<TableSection>()?.tables?.size ?: 0
        val moduleAddrs = (0 until size).map { TableAddress(store.tables.size + it) }
        importedAddresses.filterIsInstance<TableAddress>() + moduleAddrs
    }
    val memAddresses = run {
        val size = module.firstSectionOrNull<MemorySection>()?.memories?.size ?: 0
        val moduleAddrs = (0 until size).map { MemoryAddress(store.mems.size + it) }
        importedAddresses.filterIsInstance<MemoryAddress>() + moduleAddrs
    }

    val exports = module.firstSectionOrNull<ExportSection>()?.exports?.map {
        val address = when (it.description) {
            is FunctionExport -> funcAddresses[it.description.typeIndex.toInt()]
            is TableExport -> tableAddresses[it.description.tableIdx.toInt()]
            is MemoryExport -> memAddresses[it.description.memoryIdx.toInt()]
            is GlobalExport -> globalAddresses[it.description.globalIdx.toInt()]
        }
        ExportInstance(it.name, address)
    } ?: emptyList()

    val types = module.firstSectionOrNull<TypeSection>()?.types?.toList() ?: emptyList()

    val instance = SimpleModuleInstance(
        types,
        funcAddresses,
        tableAddresses,
        memAddresses,
        globalAddresses,
        exports
    )

    (0 until (module.firstSectionOrNull<FunctionSection>()?.typesIndices?.size ?: 0)).forEach {
        allocateFunction(module, it, instance)
    }
    module.firstSectionOrNull<TableSection>()?.tables?.forEach {
        allocateTable(it)
    }
    module.firstSectionOrNull<MemorySection>()?.memories?.forEach {
        allocateMemory(it)
    }
    globalValues.zip(module.firstSectionOrNull<GlobalSection>()?.globals ?: emptyArray()).forEach {
        allocateGlobal(it.second, it.first)
    }

    return instance
}