package me.sbogolepov.wvm.parser

import me.sbogolepov.wvm.raw.*

val ParserGenerator.instruction: AParser<Instruction> get() = parser {
    when ((+byte).toInt()) {
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
        else -> error("Unsupported operation")
    }
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