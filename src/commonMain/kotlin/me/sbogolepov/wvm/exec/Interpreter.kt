package me.sbogolepov.wvm.exec

import me.sbogolepov.wvm.parser.generator.FunctionType
import me.sbogolepov.wvm.parser.generator.Module
import me.sbogolepov.wvm.parser.generator.Value
import me.sbogolepov.wvm.parser.generator.ValueType
import me.sbogolepov.wvm.raw.*

class Frame(val function: Callable, val locals: MutableList<Value<*>>)

private class FrameStack : StackBase<Frame>()

private class ValueStack : StackBase<Value<*>>()

private class ReturnThrowable(val results: List<Value<*>>) : Throwable()

private class GotoThrowable(val targetLabelIndex: UInt) : Throwable()

fun ValueType.zero(): Value<*> = when (this) {
    ValueType.I32 -> Value.I32(0)
    ValueType.I64 -> Value.I64(0)
    ValueType.F32 -> Value.F32(0.0f)
    ValueType.F64 -> Value.F64(0.0)
}

class Interpreter(
    override val environment: Environment = SimpleEnvironment(),
    override val logger: Logger = SimpleLogger()
) : Machine {

    override fun error(message: String): Nothing {
        kotlin.error(message)
    }

    private val _modules = mutableMapOf<String, ModuleInstance>()

    override val modules: Map<String, ModuleInstance> = _modules

    override val store: Store = SimpleStore()

    init {
        _modules["env"] = EnvironmentModule(environment, store)
    }

    override lateinit var module: ModuleInstance

    private val callStack = FrameStack()

    private val currentFrame: Frame
        get() = callStack.peek()!!

    private val stack = ValueStack()

    private var currentLabel = 0u

    override fun investigateStack(fn: (Int, Value<*>) -> Unit) =
        stack.investigate(fn)

    override fun investigateFrameStack(fn: (Int, Frame) -> Unit) =
        callStack.investigate(fn)

    override fun eval(instructions: List<Instruction>): Value<*>? {
        instructions.map { eval(it) }
        return stack.peek()
    }

    override fun exec(entryPoint: FunctionAddress) {
        val function = store.func(entryPoint)
        require(function is Function)
        val args = (0 until (function.arity)).map { ValueType.I32.zero() }
        val locals: List<Value<*>> = function.body.locals.flatMap { entry ->
            (0u until entry.count).map { entry.type.zero() }
        }
        try {
            callStack.push(Frame(function, (args + locals).toMutableList()))
            eval(function.code.toList())
            callStack.pop()
        } catch (r: ReturnThrowable) {

        }
    }

    override fun loadModule(name: String, module: Module) {
        _modules[name] = instantiate(module)
    }

    private fun evalHost(frame: Frame) {
        throw ReturnThrowable(environment.eval(frame.function as HostFunction, frame.locals))
    }

    private fun eval(insn: Instruction) = when (insn) {
        Unreachable -> TODO()
        NOP -> TODO()
        is Block -> {
            val blockLabelIdx = currentLabel++
            try {
                eval(insn.body.toList())
            } catch (goto: GotoThrowable) {
                if (goto.targetLabelIndex != blockLabelIdx) {
                    throw goto
                }
                currentLabel--

            }
        }
        is Loop -> TODO()
        is IfElse -> TODO()
        is Br -> throw GotoThrowable(insn.labelIdx)
        is BrIf -> TODO()
        is BrTable -> TODO()
        Return -> {
            val result = ArrayList<Value<*>>(0)
            repeat ((currentFrame.function as Function).type.results.size) {
                result += stack.pop()
            }
            throw ReturnThrowable(result)
        }
        is Call -> {
            try {
                val calleeAddr = module.funcAddrs[insn.functionId.toInt()]
                val callee = store.func(calleeAddr)
                val args = (0 until callee.arity).map { stack.pop() }
                when (callee) {
                    is Function -> {
                        val locals: List<Value<*>> = callee.body.locals.flatMap {entry ->
                            (0u until entry.count).map { entry.type.zero() }
                        }
                        val frame = Frame(callee, (args + locals).toMutableList())
                        callStack.push(frame)
                        eval(callee.code.toList())
                    }
                    is HostFunction -> {
                        val frame = Frame(callee, args.toMutableList())
                        callStack.push(frame)
                        evalHost(frame)
                    }
                }
            } catch (ret: ReturnThrowable) {
                callStack.pop()
                ret.results.forEach { stack.push(it) }
            }
        }
        is CallIndirect -> TODO()
        Drop -> TODO()
        Select -> TODO()
        is LocalGet -> {
            val value = currentFrame.locals[insn.localIdx.toInt()]
            stack.push(value)
        }
        is LocalSet -> {
            val value = stack.pop()
            currentFrame.locals[insn.localIdx.toInt()] = value
        }
        is LocalTee -> TODO()
        is GlobalGet -> TODO()
        is GlobalSet -> TODO()
        MemorySize -> TODO()
        MemoryGrow -> TODO()
        is I32Load -> TODO()
        is I64Load -> TODO()
        is F32Load -> TODO()
        is F64Load -> TODO()
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
        is I32Add -> TODO()
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