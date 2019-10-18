package me.sbogolepov.wvm.exec

import me.sbogolepov.wvm.parser.generator.CodeSection
import me.sbogolepov.wvm.parser.generator.Module
import me.sbogolepov.wvm.parser.generator.Value
import me.sbogolepov.wvm.raw.*
import me.sbogolepov.wvm.utility.getDouble
import me.sbogolepov.wvm.utility.getFloat
import me.sbogolepov.wvm.utility.getInt
import me.sbogolepov.wvm.utility.getLong

class SimpleStack : Stack {

    private val stack = mutableListOf<Value<*>>()

    override fun <T : Value<*>> push(value: T) {
        stack += value
    }

    override fun <T : Value<*>> pop(): T {
        val value = stack.last()
        stack.removeAt(stack.lastIndex)
        return value as T
    }

    override fun <T : Value<*>> peek(): T = stack.last() as T
}

class SimpleMemory(val size: Int) : Memory {

    private val memory = ByteArray(size)

    override fun load(address: Address, count: Int): ByteArray {
        val start = address.index.toInt()
        return memory.copyOfRange(start, start + count)
    }

    override fun store(address: Address, value: ByteArray) {
        val start = address.index.toInt()
        value.forEachIndexed { index, byte ->
            memory[start + index] = byte
        }
    }
}

class SimpleEnvironment : Environment {
    override fun provide(functionSymbol: String): Function? {
        TODO("not implemented")
    }
}

class StackFrame(val function: Function, val args: List<Value<*>>)

class CallStack() {

    private val stack = mutableListOf<StackFrame>()

    fun push(function: Function, args: List<Value<*>>) {
        stack += StackFrame(function, args)
    }

    fun pop() {
        stack.removeAt(stack.lastIndex)
    }
}

private fun Module.findFunction(id: UInt): Function {
    val codeSection = sections.filterIsInstance<CodeSection>().first()
}

class Interpreter(
    private val module: Module
) : Machine {
    override val stack: Stack = SimpleStack()

    override val memory: Memory = SimpleMemory(65536)

    override val environment: Environment = SimpleEnvironment()

    private val callStack = CallStack()

    private class Break(var labelIdx: Int) : Exception()

    private class Return(val value: Value<*>) : Exception()

    private fun push(value: Value<*>) = stack.push(value)
    private fun <G> pop(): G = (stack.pop() as Value<G>).value

    private fun trap(): Nothing = error("TRAP")

    private fun execHost(function: Function, locals: ArrayList<Value<*>>) {

    }

    override fun exec(instructions: Sequence<Instruction>, locals: ArrayList<Value<*>>) {
        for (insn in instructions) {
            when (insn) {
                Unreachable -> trap()
                NOP -> {}
                is Block -> try {
                    exec(insn.body.asSequence(), locals)
                } catch (br: Break) {
                    if (br.labelIdx != 0) {
                        br.labelIdx -= 1
                        throw br
                    }
                }
                is Loop -> TODO()
                is IfElse -> TODO()
                is Br -> TODO()
                is BrIf -> TODO()
                is BrTable -> TODO()
                Return -> throw Return(pop())
                is Call -> try {
                    val function = module.findFunction(insn.functionId)
                    val locals = arrayListOf<Value<*>>()
                    callStack.push()
                    when (function) {
                        is HostFunction -> execHost(function, locals)
                        is UsualFunction -> exec(function.code, locals)
                    }
                } catch (ret: Return) {
                    push(ret.value)
                    callStack.pop()
                }
                is CallIndirect -> try {

                } catch (ret: Return) {

                }
                Drop -> TODO()
                Select -> TODO()

                is LocalGet -> push(locals[insn.localIdx.toInt()])
                is LocalSet -> locals[insn.localIdx.toInt()] = pop()
                is LocalTee -> push(stack.peek())

                is GlobalGet -> TODO()
                is GlobalSet -> TODO()

                MemorySize -> TODO()
                MemoryGrow -> TODO()

                is I32Load -> {
                    val arg = pop<Int>()
                    val address = Address(arg.toUInt() + insn.memArg.offset)
                    val value = (memory.load(address, 4)).getInt()
                    push(Value.I32(value))
                }
                is I64Load -> {
                    val arg = pop<Int>()
                    val address = Address(arg.toUInt() + insn.memArg.offset)
                    val value = memory.load(address, 8).getLong()
                    push(Value.I64(value))
                }
                is F32Load -> {
                    val arg = pop<Int>()
                    val address = Address(arg.toUInt() + insn.memArg.offset)
                    val value = memory.load(address, 4).getFloat()
                    push(Value.F32(value))
                }
                is F64Load -> {
                    val arg = pop<Int>()
                    val address = Address(arg.toUInt() + insn.memArg.offset)
                    val value = memory.load(address, 8).getDouble()
                    push(Value.F64(value))
                }
                is I32Load8s -> TODO()
                is I32Load8u -> TODO()
                is I32Load16s -> TODO()
                is I32Load16u -> TODO()
                is I64Load8s -> TODO()
                is I64Load8u -> TODO()
                is I64Load16s -> TODO()
                is I64Load16u -> TODO()
                is I64Load32s -> TODO()
                is I64Load32u -> TODO()

                is I32Store -> TODO()
                is I64Store -> TODO()
                is F32Store -> TODO()
                is F64Store -> TODO()
                is I32Store8 -> TODO()
                is I32Store16 -> TODO()
                is I64Store8 -> TODO()
                is I64Store16 -> TODO()
                is I64Store32 -> TODO()

                is I32Const -> stack.push(Value.I32(insn.value))
                is I64Const -> stack.push(Value.I64(insn.value))
                is F32Const -> stack.push(Value.F32(insn.value))
                is F64Const -> stack.push(Value.F64(insn.value))

                is I32Eqz -> TODO()
                is I32Eq -> TODO()
                is I32Ne -> TODO()
                is I32Lts -> TODO()
                is I32Ltu -> TODO()
                is I32Gts -> TODO()
                is I32Gtu -> TODO()
                is I32Les -> TODO()
                is I32Leu -> TODO()
                is I32Ges -> TODO()
                is I32Geu -> TODO()

                is I64Eqz -> TODO()
                is I64Eq -> TODO()
                is I64Ne -> TODO()
                is I64Lts -> TODO()
                is I64Ltu -> TODO()
                is I64Gts -> TODO()
                is I64Gtu -> TODO()
                is I64Les -> TODO()
                is I64Leu -> TODO()
                is I64Ges -> TODO()
                is I64Geu -> TODO()

                is F32Eq -> TODO()
                is F32Ne -> TODO()
                is F32Lt -> TODO()
                is F32Gt -> TODO()
                is F32Le -> TODO()
                is F32Ge -> TODO()

                is F64Eq -> TODO()
                is F64Ne -> TODO()
                is F64Lt -> TODO()
                is F64Gt -> TODO()
                is F64Le -> TODO()
                is F64Ge -> TODO()

                is I32Clz -> TODO()
                is I32Ctz -> TODO()
                is I32Popcnt -> TODO()
                is I32Add -> {
                    push(Value.I32(pop<Int>() + pop<Int>()))
                }
                is I32Sub -> TODO()
                is I32Mul -> TODO()
                is I32Divs -> TODO()
                is I32Divu -> TODO()
                is I32Rems -> TODO()
                is I32Remu -> TODO()
                is I32And -> TODO()
                is I32Or -> TODO()
                is I32Xor -> TODO()
                is I32Shl -> TODO()
                is I32Shrs -> TODO()
                is I32Shru -> TODO()
                is I32Rotl -> TODO()
                is I32Rotr -> TODO()

                is I64Clz -> TODO()
                is I64Ctz -> TODO()
                is I64Popcnt -> TODO()
                is I64Add -> TODO()
                is I64Sub -> TODO()
                is I64Mul -> TODO()
                is I64Divs -> TODO()
                is I64Divu -> TODO()
                is I64Rems -> TODO()
                is I64Remu -> TODO()
                is I64And -> TODO()
                is I64Or -> TODO()
                is I64Xor -> TODO()
                is I64Shl -> TODO()
                is I64Shrs -> TODO()
                is I64Shru -> TODO()
                is I64Rotl -> TODO()
                is I64Rotr -> TODO()

                is F32Abs -> TODO()
                is F32Neg -> TODO()
                is F32Ceil -> TODO()
                is F32Floor -> TODO()
                is F32Trunc -> TODO()
                is F32Nearest -> TODO()
                is F32Sqrt -> TODO()
                is F32Add -> TODO()
                is F32Sub -> TODO()
                is F32Mul -> TODO()
                is F32Div -> TODO()
                is F32Min -> TODO()
                is F32Max -> TODO()
                is F32Copysign -> TODO()

                is F64Abs -> TODO()
                is F64Neg -> TODO()
                is F64Ceil -> TODO()
                is F64Floor -> TODO()
                is F64Trunc -> TODO()
                is F64Nearest -> TODO()
                is F64Sqrt -> TODO()
                is F64Add -> TODO()
                is F64Sub -> TODO()
                is F64Mul -> TODO()
                is F64Div -> TODO()
                is F64Min -> TODO()
                is F64Max -> TODO()
                is F64Copysign -> TODO()

                I32WrapI64 -> TODO()
                I32TruncF32s -> TODO()
                I32TruncF32u -> TODO()
                I32TruncF64s -> TODO()
                I32TruncF64u -> TODO()

                I64ExtendI32s -> TODO()
                I64ExtendI32u -> TODO()
                I64TruncF32s -> TODO()
                I64TruncF32u -> TODO()
                I64TruncF64s -> TODO()
                I64TruncF64u -> TODO()

                F32ConvertI32s -> TODO()
                F32ConvertI32u -> TODO()
                F32ConvertI64s -> TODO()
                F32ConvertI64u -> TODO()
                F32DemoteF64 -> TODO()

                F64ConvertI32s -> TODO()
                F64ConvertI32u -> TODO()
                F64ConvertI64s -> TODO()
                F64ConvertI64u -> TODO()
                F64PromoteF64 -> TODO()

                I32ReinterpretF32 -> TODO()
                I64ReinterpretF64 -> TODO()
                F32ReinterpretI32 -> TODO()
                F64ReinterpretI64 -> TODO()
            }
        }
    }
}