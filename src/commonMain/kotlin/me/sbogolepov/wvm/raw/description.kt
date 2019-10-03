package me.sbogolepov.wvm.parser.generator

import me.sbogolepov.wvm.raw.ConstantExpression


enum class ValueType {
    I32, I64, F32, F64
}

sealed class Value {
    class I32(value: Int): Value()
    class I64(value: Long): Value()
    class F32(value: Float): Value()
    class F64(value: Double): Value()
}

sealed class Result {
    object Empty : Result()
    class ValueResult(value: Value) : Result()
}

class FunctionType(val parameters: Array<ValueType>, val results: Array<ValueType>)

sealed class Limit(val min: UInt) {
    class Open(min: UInt) : Limit(min)
    class Closed(min: UInt, val max: UInt) : Limit(min)
}

class Memory(val limit: Limit)

// TODO: Rename to TableType?
class Table(val elementType: ElementType, val limit: Limit) {
    enum class ElementType {
        FuncRef
    }
}

class Global(val value: Value, val mutable: Boolean)

class GlobalType(val valueType: ValueType, val mutable: Boolean)

class Import(val module: String, val name: String, val description: ImportDescription)

sealed class ImportDescription
class FunctionImport(val typeIndex: UInt) : ImportDescription()
class TableImport(val table: Table) : ImportDescription()
class MemoryImport(val memory: Memory) : ImportDescription()
class GlobalImport(val globalType: GlobalType) : ImportDescription()

sealed class ExportDescription
class FunctionExport(val typeIndex: UInt) : ExportDescription()
class TableExport(val tableIdx: UInt) : ExportDescription()
class MemoryExport(val memoryIdx: UInt) : ExportDescription()
class GlobalExport(val globalIdx: UInt) : ExportDescription()

class ElementSegment(val tableIdx: UInt, val offset: ConstantExpression, val init: List<FunctionType>)


class SectionHeader(val id: Byte, val sizeInBytes: UInt)

sealed class Section
class CustomSection(val name: String, val data: ByteArray) : Section()
class TypeSection(val types: Array<FunctionType>) : Section()
class ImportSection(val imports: Array<Import>) : Section()
class FunctionSection(val typesIndices: Array<UInt>) : Section()
class TableSection(val tables: Array<Table>) : Section()
class MemorySection(val memories: Array<Memory>) : Section()
class GlobalSection(val globals: Array<Global>) : Section()
class ExportSection(val exports: Array<ExportDescription>) : Section()
class StartSection(val start: UInt) : Section()
class ElementSection() : Section()
class CodeSection() : Section()
class DataSection() : Section()

