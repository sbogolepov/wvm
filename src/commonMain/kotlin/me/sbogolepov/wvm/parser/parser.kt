@file:Suppress("EXPERIMENTAL_API_USAGE")

package me.sbogolepov.wvm.parser

import me.sbogolepov.wvm.io.*
import me.sbogolepov.wvm.parser.generator.*

// TODO: Think about DSL for parsing
data class ParseResult<T>(val data: T, val bytesRead: Int)

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

@ExperimentalStdlibApi
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

@ExperimentalStdlibApi
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

@ExperimentalStdlibApi
fun RawDataReader.customSection(sectionHeader: SectionHeader): CustomSection {
    val (name, bytesRead) = name()
    return CustomSection(name, readBytes(sectionHeader.sizeInBytes.toInt() - bytesRead))
}

@ExperimentalStdlibApi
fun RawDataReader.typeSection(sectionHeader: SectionHeader): ParseResult<TypeSection> {
    val (functions, read) = vector { functionType() }
    return ParseResult(TypeSection(functions), read)
}

fun RawDataReader.importSection(sectionHeader: SectionHeader): ImportSection {}

fun RawDataReader.functionSection(sectionHeader: SectionHeader): FunctionSection {}

fun RawDataReader.tableSection(sectionHeader: SectionHeader): TableSection {}

fun RawDataReader.memorySection(sectionHeader: SectionHeader): MemorySection {}

fun RawDataReader.globalSection(sectionHeader: SectionHeader): GlobalSection {}

fun RawDataReader.exportSection(sectionHeader: SectionHeader): ExportSection {}

fun RawDataReader.startSection(sectionHeader: SectionHeader): StartSection {}

fun RawDataReader.elementSection(sectionHeader: SectionHeader): ElementSection {}

fun RawDataReader.codeSection(sectionHeader: SectionHeader): CodeSection {}

fun RawDataReader.dataSection(sectionHeader: SectionHeader): DataSection {}


@ExperimentalStdlibApi
fun RawDataReader.sectionByHeader(sectionHeader: SectionHeader): Section =
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