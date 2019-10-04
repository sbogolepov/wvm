package me.sbogolepov.wvm.parser

import me.sbogolepov.wvm.io.*
import me.sbogolepov.wvm.parser.generator.*

// TODO: Think about DSL for parsing
data class ParseResult<out T>(val data: T, val bytesRead: Int)

val WASM_MAGIC = byteArrayOf(0x00, 0x61, 0x73, 0x6d)

fun RawDataReader.magic(): ByteArray = readBytes(4)

fun RawDataReader.wasmVersion(): UInt = readUInt32()

inline fun <reified T> RawDataReader.vector(element: RawDataReader.() -> ParseResult<T>): ParseResult<Array<T>> {
    val (vectorSize, size) = u32()
    var read = size
    val data = Array(vectorSize.toInt()) {
        element().let {
            read += it.bytesRead
            it.data
        }
    }
    return ParseResult(data, read)
}

fun RawDataReader.name(): ParseResult<String> {
    val (vector, read) = vector { byte() }
    return ParseResult(vector.toByteArray().decodeToString(), read)
}

fun RawDataReader.valueType(): ParseResult<ValueType> {
    val type = when (val byte = readByte().toInt()) {
        0x7f -> ValueType.I32
        0x7e -> ValueType.I64
        0x7d -> ValueType.F32
        0x7c -> ValueType.F64
        else -> error("Unexpected value type: ${byte.toString(16)}")
    }
    return ParseResult(type, 1)
}

fun RawDataReader.limits(): ParseResult<Limit> {
    return if (readByte().toInt() == 0) {
        val (min, r0) = u32()
        ParseResult(Limit.Open(min), r0 + 1)
    } else {
        val (min, r0) = u32()
        val (max, r1) = u32()
        ParseResult(Limit.Closed(min, max), r0 + r1 + 1)
    }
}

fun RawDataReader.tableType(): ParseResult<Table> {
    check(readByte().toInt() == 0x70) { "Only FuncRef types are supported" }
    val (limits, r0) = limits()
    return ParseResult(Table(Table.ElementType.FuncRef, limits), r0+1)
}

fun RawDataReader.globalType(): ParseResult<GlobalType> {
    val (valueType, r0) = valueType()
    return ParseResult(GlobalType(valueType, readByte().toInt() == 0), r0 + 1)
}

fun RawDataReader.importDescription(): ParseResult<ImportDescription> {
    val (importDesc, r1) = when (val byte = readByte().toInt()) {
        0x00 -> {
            val (typeIdx, r0) = u32()
            FunctionImport(typeIdx) to r0
        }
        0x01 -> {
           val (tableType, r0) = tableType()
            TableImport(tableType) to r0
        }
        0x02 -> {
            val (limits, r0) = limits()
            MemoryImport(Memory(limits)) to r0
        }
        0x03 -> {
            val (globalType, r0) = globalType()
            GlobalImport(globalType) to r0
        }
        else -> error("Unexpected import description: ${byte.toString(16)}")
    }
    return ParseResult(importDesc, r1 + 1)
}

fun RawDataReader.import(): ParseResult<Import> {
    val (module, r1) = name()
    val (name, r2) = name()
    val (importDesc, r3) = importDescription()
    return ParseResult(Import(module, name, importDesc), r1 + r2 + r3)
}

fun RawDataReader.functionType(): ParseResult<FunctionType> {
    check(readByte().toInt() == 0x60) { "Function type should start with 0x60 byte" }
    val (parameters, parametersBytes) = vector { valueType() }
    val (returns, returnsBytes) = vector { valueType() }
    val read = 1 + parametersBytes + returnsBytes
    return ParseResult(FunctionType(parameters, returns), read)
}

fun RawDataReader.sectionHeader(): ParseResult<SectionHeader> {
    val (sectionId, r1) = byte()
    val (sizeInBytes, r2) = u32()
    return ParseResult(SectionHeader(sectionId, sizeInBytes), r1 + r2)
}

fun RawDataReader.customSection(sectionHeader: SectionHeader): ParseResult<CustomSection> {
    val (name, bytesRead) = name()
    val section = CustomSection(name, readBytes(sectionHeader.sizeInBytes.toInt() - bytesRead))
    return ParseResult(section, sectionHeader.sizeInBytes.toInt())
}

fun RawDataReader.typeSection(sectionHeader: SectionHeader): ParseResult<TypeSection> {
    val (functions, read) = vector { functionType() }
    return ParseResult(TypeSection(functions), read)
}

fun RawDataReader.importSection(sectionHeader: SectionHeader): ParseResult<ImportSection> {
    val (imports, read) = vector { import() }
    return ParseResult(ImportSection(imports), read)
}

fun RawDataReader.functionSection(sectionHeader: SectionHeader): ParseResult<FunctionSection> { TODO() }

fun RawDataReader.tableSection(sectionHeader: SectionHeader): ParseResult<TableSection> {TODO()}

fun RawDataReader.memorySection(sectionHeader: SectionHeader): ParseResult<MemorySection> {TODO()}

fun RawDataReader.globalSection(sectionHeader: SectionHeader): ParseResult<GlobalSection> {TODO()}

fun RawDataReader.exportSection(sectionHeader: SectionHeader): ParseResult<ExportSection> {TODO()}

fun RawDataReader.startSection(sectionHeader: SectionHeader): ParseResult<StartSection> {TODO()}

fun RawDataReader.elementSection(sectionHeader: SectionHeader): ParseResult<ElementSection> {TODO()}

fun RawDataReader.codeSection(sectionHeader: SectionHeader): ParseResult<CodeSection> {TODO()}

fun RawDataReader.dataSection(sectionHeader: SectionHeader): ParseResult<DataSection> {TODO()}

fun RawDataReader.sectionByHeader(sectionHeader: SectionHeader): ParseResult<Section> =
    when (sectionHeader.id.toInt()) {
        0 -> customSection(sectionHeader)
        1 -> typeSection(sectionHeader)
        2 -> importSection(sectionHeader)
        3 -> functionSection(sectionHeader)
        4 -> tableSection(sectionHeader)
        5 -> memorySection(sectionHeader)
        6 -> globalSection(sectionHeader)
        7 -> exportSection(sectionHeader)
        8 -> startSection(sectionHeader)
        9 -> elementSection(sectionHeader)
        10 -> codeSection(sectionHeader)
        11 -> dataSection(sectionHeader)
        else -> error("Unsupported section ID = ${sectionHeader.id}")
    }