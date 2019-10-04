package me.sbogolepov.wvm.raw

import me.sbogolepov.wvm.parser.generator.ValueType

sealed class Instruction
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

object Drop : Instruction()
object Select : Instruction()

class LocalGet(val localIdx: UInt) : Instruction()
class LocalSet(val localIdx: UInt) : Instruction()
class LocalTee(val localIdx: UInt) : Instruction()
class GlobalGet(val globalIdx: UInt) : Instruction()
class GlobalSet(val globalIdx: UInt) : Instruction()

