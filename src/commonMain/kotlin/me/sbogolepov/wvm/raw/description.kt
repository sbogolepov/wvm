package me.sbogolepov.wvm.parser.generator

import me.sbogolepov.wvm.raw.ConstantExpression
import me.sbogolepov.wvm.raw.Expression


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

sealed class Limits(val min: UInt) {
    class Open(min: UInt) : Limits(min)
    class Closed(min: UInt, val max: UInt) : Limits(min)
}

class Memory(val limits: Limits)

class TableType(val elementType: ElementType, val limits: Limits) {
    enum class ElementType {
        FuncRef
    }
}

class Global(val type: GlobalType, val expression: Expression)

class GlobalType(val valueType: ValueType, val mutable: Boolean)

class Import(val module: String, val name: String, val description: ImportDescription)

sealed class ImportDescription
class FunctionImport(val typeIndex: UInt) : ImportDescription()
class TableImport(val table: TableType) : ImportDescription()
class MemoryImport(val memory: Memory) : ImportDescription()
class GlobalImport(val globalType: GlobalType) : ImportDescription()

class Export(val name: String, val description: ExportDescription)

sealed class ExportDescription
class FunctionExport(val typeIndex: UInt) : ExportDescription()
class TableExport(val tableIdx: UInt) : ExportDescription()
class MemoryExport(val memoryIdx: UInt) : ExportDescription()
class GlobalExport(val globalIdx: UInt) : ExportDescription()

class Element(val tableIdx: UInt, val offset: Expression, val init: Array<FunctionType>)

class Data(val memIdx: UInt, val offset: Expression, val init: ByteArray)

class SectionHeader(val id: Byte, val sizeInBytes: UInt)

sealed class Section
class CustomSection(val name: String, val data: ByteArray) : Section()
class TypeSection(val types: Array<FunctionType>) : Section()
class ImportSection(val imports: Array<Import>) : Section()
class FunctionSection(val typesIndices: Array<UInt>) : Section()
class TableSection(val tables: Array<TableType>) : Section()
class MemorySection(val memories: Array<Memory>) : Section()
class GlobalSection(val globals: Array<Global>) : Section()
class ExportSection(val exports: Array<Export>) : Section()
class StartSection(val start: UInt) : Section()
class ElementSection(val elements: Array<Element>) : Section()
class CodeSection() : Section()
class DataSection(val data: Array<Data>) : Section()

