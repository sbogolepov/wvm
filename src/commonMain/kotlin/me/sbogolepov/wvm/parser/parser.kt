package me.sbogolepov.wvm.parser

import me.sbogolepov.wvm.io.*
import me.sbogolepov.wvm.parser.generator.*
import me.sbogolepov.wvm.raw.Expression

val WASM_MAGIC = byteArrayOf(0x00, 0x61, 0x73, 0x6d)

fun RawDataReader.magic(): ByteArray = readBytes(4)

fun RawDataReader.wasmVersion(): UInt = readUInt32()

val ParserGenerator.name get() = parser<String> {
    val bytes = +vector(byte)
    bytes.toByteArray().decodeToString()
}

val ParserGenerator.valueType get() = parser<ValueType> {
    when (val byte = (+byte).toInt()) {
        0x7f -> ValueType.I32
        0x7e -> ValueType.I64
        0x7d -> ValueType.F32
        0x7c -> ValueType.F64
        else -> error("Unexpected value type: ${byte.toString(16)}")
    }
}

val ParserGenerator.limits get() = parser<Limits> {
    if (+byte == 0.toByte()) {
        Limits.Open(+u32)
    } else {
        Limits.Closed(+u32, +u32)
    }
}

val ParserGenerator.tableType get() = parser<TableType> {
    check(+byte == 0x70.toByte()) { "Only FuncRef types are supported" }
    TableType(TableType.ElementType.FuncRef, +limits)
}

val ParserGenerator.globalType get() = parser<GlobalType> {
    GlobalType(+valueType, +byte == 0.toByte())
}

val ParserGenerator.importDescription get() = parser<ImportDescription> {
    when (val byte = (+byte).toInt()) {
        0x00 -> FunctionImport(+u32)
        0x01 -> TableImport(+tableType)
        0x02 -> MemoryImport(Memory(+limits))
        0x03 -> GlobalImport(+globalType)
        else -> error("Unexpected import description: ${byte.toString(16)}")
    }
}

val ParserGenerator.import get() = parser<Import> {
    Import(+name, +name, +importDescription)
}

val ParserGenerator.exportDescription get() = parser<ExportDescription> {
    when (val byte = (+byte).toInt()) {
        0x00 -> FunctionExport(+u32)
        0x01 -> TableExport(+u32)
        0x02 -> MemoryExport(+u32)
        0x03 -> GlobalExport(+u32)
        else -> error("Unexpected import description: ${byte.toString(16)}")
    }
}

val ParserGenerator.export get() = parser<Export> {
    Export(+name, +exportDescription)
}

val ParserGenerator.functionType get() = parser<FunctionType> {
    check(+byte == 0x60.toByte()) { "Function type should start with 0x60 byte" }
    FunctionType(+vector(valueType), +vector(valueType))
}

val ParserGenerator.global get() = parser<Global> {
    Global(+globalType, +expr)
}

val ParserGenerator.element get() = parser<Element> {
    Element(+u32, +expr, +vector(functionType))
}

val ParserGenerator.data get() = parser<Data> {
    Data(+u32, +expr, (+vector(byte)).toByteArray())
}

val ParserGenerator.sectionHeader get() = parser<SectionHeader> {
    SectionHeader(+byte, +u32)
}

fun ParserGenerator.customSection(sectionSize: Int) = parser<CustomSection> {
    val name = +name
    val bytesInSection = sectionSize - bytesRead
    val bytes: List<Byte> = (0..bytesInSection).map { +byte }
    CustomSection(name, bytes.toByteArray())
}

val ParserGenerator.typeSection get() = parser<TypeSection> {
    TypeSection(+vector(functionType))
}

val ParserGenerator.importSection get() = parser<ImportSection> {
    ImportSection(+vector(import))
}

val ParserGenerator.functionSection get() = parser<FunctionSection> {
    FunctionSection(+vector(u32))
}

val ParserGenerator.tableSection get() = parser<TableSection> {
    TableSection(+vector(tableType))
}

val ParserGenerator.memorySection get() = parser<MemorySection> {
    MemorySection((+vector(limits)).map { Memory(it) }.toTypedArray())
}

val ParserGenerator.globalSection get() = parser<GlobalSection> {
    GlobalSection(+vector(global))
}

val ParserGenerator.exportSection get() = parser<ExportSection> {
    ExportSection(+vector(export))
}

val ParserGenerator.startSection get() = parser<StartSection> {
    StartSection(+u32)
}

val ParserGenerator.elementSection get() = parser<ElementSection> {
    ElementSection(+vector(element))
}

val ParserGenerator.codeSection get() = parser<CodeSection> {
    CodeSection()
}

val ParserGenerator.dataSection get() = parser<DataSection> {
    DataSection(+vector(data))
}

val ParserGenerator.section get() = parser<Section> {
    val header = +sectionHeader
    +when (header.id.toInt()) {
        0 -> customSection(header.sizeInBytes.toInt())
        1 -> typeSection
        2 -> importSection
        3 -> functionSection
        4 -> tableSection
        5 -> memorySection
        6 -> globalSection
        7 -> exportSection
        8 -> startSection
        9 -> elementSection
        10 -> codeSection
        11 -> dataSection
        else -> error("Unsupported section ID = ${header.id}")
    }
}

val ParserGenerator.expr get() = parser<Expression> {
    TODO()
}
