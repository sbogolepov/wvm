package me.sbogolepov.wvm.raw

import me.sbogolepov.wvm.parser.generator.ValueType

sealed class Instruction : Expression
object Unreachable : Instruction()
object NOP : Instruction()


sealed class ResultType {
    object Empty : ResultType()
    class Value(val valueType: ValueType) : ResultType()
}

class Block(val type: ResultType, val body: Array<Instruction>) : Instruction()
class Loop(val type: ResultType, val body: Array<Instruction>) : Instruction()
class IfElse(val type: ResultType, val tru: Array<Instruction>, val fls: Array<Instruction>) : Instruction()

class Br(val labelIdx: UInt): Instruction()
class BrIf(val labelIdx: UInt): Instruction()
class BrTable(val table: Array<UInt>, val default: UInt): Instruction()
object Return: Instruction()
class Call(val function: UInt): Instruction()
class CallIndirect(val typeIdx: UInt): Instruction()
