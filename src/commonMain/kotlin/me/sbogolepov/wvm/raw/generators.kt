package me.sbogolepov.wvm.raw

val memInsnRange = 0x28..0x40
val integralCmpInsnRange = 0x45..0x5a
val floatCmpInsnsRange = 0x5b..0x66
val integralMathRange = 0x67..0x8a
val floatMathRange = 0x8b..0xA6
val conversionsRange = 0xA7..0xBF

fun generateMemoryInstructionName(type: String, operation: String): String = """
    ${type.capitalize()}${operation.capitalize()}
""".trimIndent()

fun generateClassFromMemInsnName(name: String): String = """
    class ${name}(val memArg: MemArg) : Instruction()
""".trimIndent()

fun generateConversionName(from: String, op: String, to: String, variant: String = "") = """
    ${from.capitalize()}${op.capitalize()}${to.capitalize()}$variant
""".trimIndent()

fun generateMemoryOperations(): List<String> {
    val classes = mutableListOf<String>()

    for (op in arrayOf("Load", "Store")) {
        for (type in arrayOf(32, 64)) {
            classes += generateMemoryInstructionName("I$type", op)
        }
        for (type in arrayOf(32, 64)) {
            classes += generateMemoryInstructionName("F$type", op)
        }
        for (type in arrayOf(32, 64)) {
            for (j in arrayOf(8, 16, 32)) {
                if (j >= type) continue
                when (op) {
                    "Load" -> {
                        classes += generateMemoryInstructionName("I$type", "$op${j}s")
                        classes += generateMemoryInstructionName("I$type", "$op${j}u")
                    }
                    "Store" -> {
                        classes += generateMemoryInstructionName("I$type", "$op${j}")
                    }
                }
            }
        }
    }
    return classes.toList()
}

fun generateIntegralCmpInsns(): List<String> {
    val ops = listOf("Eqz", "Eq", "Ne", "Lts", "Ltu", "Gts", "Gtu", "Les", "Leu", "Ges", "Geu")
    return listOf("I32", "I64").flatMap { type -> ops.map { "$type$it" } }
}

fun generateFloatCmpInsns(): List<String> {
    val ops = listOf("Eq", "Ne", "Lt", "Gt", "Le", "Ge")
    return listOf("F32", "F64").flatMap { type -> ops.map { "$type$it" } }
}

fun generateIntegralMathInsn(): List<String> {
    val ops = listOf(
        "clz", "ctz", "popcnt", "add", "sub", "mul",
        "divs", "divu", "rems", "remu",
        "and", "or", "xor",
        "shl", "shrs", "shru",
        "rotl", "rotr"
    )
    return listOf("I32", "I64").flatMap { type -> ops.map { "$type${it.capitalize()}" } }
}

val floatMathInsns = run {
    val ops = listOf(
        "abs", "neg", "ceil", "floor", "trunc", "nearest", "sqrt",
        "add", "sub", "mul", "div", "min", "max", "copysign"
    )
    listOf("F32", "F64").flatMap { type -> ops.map { "$type${it.capitalize()}" } }
}

fun generateParsingSwitch(
    range: IntRange,
    operations: List<String>,
    operationGenFn: (String) -> String
): List<String> {
    val body = mutableListOf<String>()
    val opBytes = range.toList()
    operations.forEachIndexed { index, op ->
        body += "0x${opBytes[index].toString(16)} -> ${operationGenFn(op)}"
    }
    body += "else -> error(\"\")"
    return body
}

fun generateIntegralCmpClasses(): List<String> =
    generateIntegralCmpInsns().map { "class $it() : Instruction()" }

fun generateIntegralCmpParser(): List<String> =
    generateParsingSwitch(integralCmpInsnRange, generateIntegralCmpInsns()) { "$it()" }

fun generateFloatCmpClasses(): List<String> =
    generateFloatCmpInsns().map { "class $it() : Instruction()" }

fun generateFloatCmpParser(): List<String> =
    generateParsingSwitch(floatCmpInsnsRange, generateFloatCmpInsns()) { "$it()" }

fun generateMemOpClasses(): List<String> =
    generateMemoryOperations().map(::generateClassFromMemInsnName)

fun generateMemOpParser(): List<String> =
    generateParsingSwitch(memInsnRange, generateMemoryOperations()) {"$it(+memArg)" }

fun generateIntegralMathClasses() =
    generateIntegralMathInsn().map { "class $it() : Instruction()" }

fun generateIntegralMathParser() =
    generateParsingSwitch(integralMathRange, generateIntegralMathInsn()) { "$it()" }

val floatMathClasses = floatMathInsns.map { "class $it() : Instruction()" }

val floatMathParser = generateParsingSwitch(floatMathRange, floatMathInsns) { "$it()" }

val conversions = listOf(
    generateConversionName("i32", "wrap", "i64"),
    generateConversionName("i32", "trunc", "f32", "s"),
    generateConversionName("i32", "trunc", "f32", "u"),
    generateConversionName("i32", "trunc", "f64", "s"),
    generateConversionName("i32", "trunc", "f64", "u"),
    generateConversionName("i64", "extend", "i32", "s"),
    generateConversionName("i64", "extend", "i32", "u"),
    generateConversionName("i64", "trunc", "f32", "s"),
    generateConversionName("i64", "trunc", "f32", "u"),
    generateConversionName("i64", "trunc", "f64", "s"),
    generateConversionName("i64", "trunc", "f64", "u"),
    generateConversionName("f32", "convert", "i32", "s"),
    generateConversionName("f32", "convert", "i32", "u"),
    generateConversionName("f32", "convert", "i64", "s"),
    generateConversionName("f32", "convert", "i64", "u"),
    generateConversionName("f32", "demote", "f64"),
    generateConversionName("f64", "convert", "i32", "s"),
    generateConversionName("f64", "convert", "i32", "u"),
    generateConversionName("f64", "convert", "i64", "s"),
    generateConversionName("f64", "convert", "i64", "u"),
    generateConversionName("f64", "promote", "f64"),
    generateConversionName("i32", "reinterpret", "f32"),
    generateConversionName("i64", "reinterpret", "f64"),
    generateConversionName("f32", "reinterpret", "i32"),
    generateConversionName("f64", "reinterpret", "i64")
)

val conversionsClasses = conversions.map { "object $it : Instruction()" }
val conversionsParser = generateParsingSwitch(conversionsRange, conversions) { it }