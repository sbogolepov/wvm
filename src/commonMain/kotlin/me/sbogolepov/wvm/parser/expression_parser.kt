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

        // Constant
        0x41 -> I32Const(+i32)
        0x42 -> I64Const(+i64)
        0x43 -> F32Const(+f32)
        0x44 -> F64Const(+f64)

        in integralCmpInsnRange -> +integralCmp(byte)

        in floatCmpInsnsRange -> +floatCmp(byte)

        in integralMathRange -> +integralMath(byte)

        in floatMathRange -> +floatMath(byte)

        in conversionsRange -> +conversion(byte)

        else -> error("Unsupported operation ${byte.toString(16)}")
    }
}

fun ParserGenerator.conversion(byte: Int) = parser<Instruction> {
    when (byte) {
        0xa7 -> I32WrapI64
        0xa8 -> I32TruncF32s
        0xa9 -> I32TruncF32u
        0xaa -> I32TruncF64s
        0xab -> I32TruncF64u
        0xac -> I64ExtendI32s
        0xad -> I64ExtendI32u
        0xae -> I64TruncF32s
        0xaf -> I64TruncF32u
        0xb0 -> I64TruncF64s
        0xb1 -> I64TruncF64u
        0xb2 -> F32ConvertI32s
        0xb3 -> F32ConvertI32u
        0xb4 -> F32ConvertI64s
        0xb5 -> F32ConvertI64u
        0xb6 -> F32DemoteF64
        0xb7 -> F64ConvertI32s
        0xb8 -> F64ConvertI32u
        0xb9 -> F64ConvertI64s
        0xba -> F64ConvertI64u
        0xbb -> F64PromoteF64
        0xbc -> I32ReinterpretF32
        0xbd -> I64ReinterpretF64
        0xbe -> F32ReinterpretI32
        0xbf -> F64ReinterpretI64
        else -> error("")
    }
}

fun ParserGenerator.floatMath(byte: Int) = parser<Instruction> {
    when (byte) {
        0x8b -> F32Abs()
        0x8c -> F32Neg()
        0x8d -> F32Ceil()
        0x8e -> F32Floor()
        0x8f -> F32Trunc()
        0x90 -> F32Nearest()
        0x91 -> F32Sqrt()
        0x92 -> F32Add()
        0x93 -> F32Sub()
        0x94 -> F32Mul()
        0x95 -> F32Div()
        0x96 -> F32Min()
        0x97 -> F32Max()
        0x98 -> F32Copysign()
        0x99 -> F64Abs()
        0x9a -> F64Neg()
        0x9b -> F64Ceil()
        0x9c -> F64Floor()
        0x9d -> F64Trunc()
        0x9e -> F64Nearest()
        0x9f -> F64Sqrt()
        0xa0 -> F64Add()
        0xa1 -> F64Sub()
        0xa2 -> F64Mul()
        0xa3 -> F64Div()
        0xa4 -> F64Min()
        0xa5 -> F64Max()
        0xa6 -> F64Copysign()
        else -> error("")

    }
}

fun ParserGenerator.integralMath(byte: Int) = parser<Instruction> {
    when (byte) {
        0x67 -> I32Clz()
        0x68 -> I32Ctz()
        0x69 -> I32Popcnt()
        0x6a -> I32Add()
        0x6b -> I32Sub()
        0x6c -> I32Mul()
        0x6d -> I32Divs()
        0x6e -> I32Divu()
        0x6f -> I32Rems()
        0x70 -> I32Remu()
        0x71 -> I32And()
        0x72 -> I32Or()
        0x73 -> I32Xor()
        0x74 -> I32Shl()
        0x75 -> I32Shrs()
        0x76 -> I32Shru()
        0x77 -> I32Rotl()
        0x78 -> I32Rotr()
        0x79 -> I64Clz()
        0x7a -> I64Ctz()
        0x7b -> I64Popcnt()
        0x7c -> I64Add()
        0x7d -> I64Sub()
        0x7e -> I64Mul()
        0x7f -> I64Divs()
        0x80 -> I64Divu()
        0x81 -> I64Rems()
        0x82 -> I64Remu()
        0x83 -> I64And()
        0x84 -> I64Or()
        0x85 -> I64Xor()
        0x86 -> I64Shl()
        0x87 -> I64Shrs()
        0x88 -> I64Shru()
        0x89 -> I64Rotl()
        0x8a -> I64Rotr()
        else -> error("")
    }
}

fun ParserGenerator.floatCmp(byte: Int): AParser<Instruction> = parser {
    when (byte) {
        0x5b -> F32Eq()
        0x5c -> F32Ne()
        0x5d -> F32Lt()
        0x5e -> F32Gt()
        0x5f -> F32Le()
        0x60 -> F32Ge()
        0x61 -> F64Eq()
        0x62 -> F64Ne()
        0x63 -> F64Lt()
        0x64 -> F64Gt()
        0x65 -> F64Le()
        0x66 -> F64Ge()
        else -> error("")
    }
}

fun ParserGenerator.integralCmp(byte: Int): AParser<Instruction> = parser {
    when (byte) {
        0x45 -> I32Eqz()
        0x46 -> I32Eq()
        0x47 -> I32Ne()
        0x48 -> I32Lts()
        0x49 -> I32Ltu()
        0x4a -> I32Gts()
        0x4b -> I32Gtu()
        0x4c -> I32Les()
        0x4d -> I32Leu()
        0x4e -> I32Ges()
        0x4f -> I32Geu()
        0x50 -> I64Eqz()
        0x51 -> I64Eq()
        0x52 -> I64Ne()
        0x53 -> I64Lts()
        0x54 -> I64Ltu()
        0x55 -> I64Gts()
        0x56 -> I64Gtu()
        0x57 -> I64Les()
        0x58 -> I64Leu()
        0x59 -> I64Ges()
        0x5a -> I64Geu()
        else -> error("")
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
        0x3F -> MemorySize.also { eat() }
        0x40 -> MemoryGrow.also { eat() }
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