package me.sbogolepov.wvm.parser

import me.sbogolepov.wvm.raw.*

val ParserGenerator.expr get() = parser<Expression> {
    ExpressionImpl(+instructions)
}

val ParserGenerator.instruction: AParser<Instruction> get() = parser {
    when (val byte = (+byte).toInt()) {
        // Control
        0x00 -> Unreachable
        0x01 -> NOP
        0x02 -> +block
        0x03 -> +loop
        0x04 -> +ifElse
        0x0c -> +br
        0x0d -> +brIf
        0x0e -> +brTable
        0x0f -> Return
        0x10 -> +call
        0x11 -> +callIndirect

        // Parametric
        0x1A -> Drop
        0x1B -> Select

        // Variables
        0x20 -> LocalGet(+u32)
        0x21 -> LocalSet(+u32)
        0x22 -> LocalTee(+u32)
        0x23 -> GlobalGet(+u32)
        0x24 -> GlobalSet(+u32)

        // Memory
        in memInsnRange -> +memoryInstruction(byte)

        else -> error("Unsupported operation ${byte.toString(16)}")
    }
}

fun ParserGenerator.memoryInstruction(byte: Int): AParser<Instruction> = parser {
    when (byte) {
        0x28 -> I32Load(+memArg)
        0x29 -> I64Load(+memArg)
        0x2a -> F32Load(+memArg)
        0x2b -> F64Load(+memArg)
        0x2c -> I32Load8s(+memArg)
        0x2d -> I32Load8u(+memArg)
        0x2e -> I32Load16s(+memArg)
        0x2f -> I32Load16u(+memArg)
        0x30 -> I64Load8s(+memArg)
        0x31 -> I64Load8u(+memArg)
        0x32 -> I64Load16s(+memArg)
        0x33 -> I64Load16u(+memArg)
        0x34 -> I64Load32s(+memArg)
        0x35 -> I64Load32u(+memArg)
        0x36 -> I32Store(+memArg)
        0x37 -> I64Store(+memArg)
        0x38 -> F32Store(+memArg)
        0x39 -> F64Store(+memArg)
        0x3a -> I32Store8(+memArg)
        0x3b -> I32Store16(+memArg)
        0x3c -> I64Store8(+memArg)
        0x3d -> I64Store16(+memArg)
        0x3e -> I64Store32(+memArg)
        0x3F -> MemorySize
        0x40 -> MemoryGrow
        else -> error("Unsupported memory instruction ${byte.toString(16)}")
    }
}

val ParserGenerator.memArg get() = parser<MemArg> {
    MemArg(+u32, +u32)
}

val ParserGenerator.call get() = parser<Call> {
    Call(+u32)
}

val ParserGenerator.callIndirect get() = parser<CallIndirect> {
    CallIndirect(+u32).also { eat() }
}

val ParserGenerator.br get() = parser<Br> {
    Br(+u32)
}

val ParserGenerator.brIf get() = parser<BrIf> {
    BrIf(+u32)
}

val ParserGenerator.brTable get() = parser<BrTable> {
    BrTable(+vector(u32), +u32)
}

val ParserGenerator.block get() = parser<Block> {
    Block(+blockType, +instructions)
}

val ParserGenerator.loop get() = parser<Loop> {
    Loop(+blockType, +instructions)
}

val ParserGenerator.ifElse get() = parser<IfElse> {
    val type = +blockType
    var hasElseBranch = false
    val truBranch = +parseWhile(instruction) {
        hasElseBranch = it == 0x05.toByte()
        it != 0x0b.toByte() && it != 0x05.toByte()
    }
    val elseBranch = if (hasElseBranch) {
        +instructions
    } else {
        emptyArray()
    }
    IfElse(type, truBranch.toTypedArray(), elseBranch)
}

val ParserGenerator.blockType get() = parser<ResultType> {
    when (peek()) {
        0x40.toByte() -> ResultType.Empty.also { eat() }
        else -> ResultType.Value(+valueType)
    }
}

val ParserGenerator.instructions get() = parser<Array<Instruction>> {
    val instructions = +parseWhile(instruction) { it != 0x0b.toByte() }
    instructions.toTypedArray()
}