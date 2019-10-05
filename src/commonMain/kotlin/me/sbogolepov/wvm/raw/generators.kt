package me.sbogolepov.wvm.raw

val memInsnRange = 0x28..0x40

val integralCmpInsnRange = 0x45..0x5a

val floatCmpInsnsRange = 0x5b..0x66

fun generateMemoryInstructionName(type: String, operation: String): String = """
    ${type.capitalize()}${operation.capitalize()}
""".trimIndent()

fun generateClassFromMemInsnName(name: String): String = """
    class ${name}(val memArg: MemArg) : Instruction()
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