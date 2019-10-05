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

fun generateIntegralCmpInsns(type: String): List<String> {
    val ops = listOf("Eqz", "Eq", "Ne", "Lts", "Ltu", "Gts", "Gtu", "Les", "Leu", "Ges", "Geu")
    return ops.map { "$type$it" }
}

fun generateIntegralCmpClasses(): List<String> =
    listOf("I32", "I64").flatMap { generateIntegralCmpInsns(it) }.map { "class $it() : Instruction()" }

fun generateIntegralCmpParser(): List<String> {
    val names = listOf("I32", "I64").flatMap { generateIntegralCmpInsns(it) }
    val body = mutableListOf<String>()
    val opBytes = integralCmpInsnRange.toList()
    names.forEachIndexed { index, op ->
        body += "0x${opBytes[index].toString(16)} -> $op()"
    }
    return body
}

fun generateFloatCmpInsns(type: String): List<String> {
    val ops = listOf("Eq", "Ne", "Lt", "Gt", "Le", "Ge")
    return ops.map { "$type$it" }
}

fun generateFloatCmpClasses(): List<String> =
    listOf("F32", "F64").flatMap { generateFloatCmpInsns(it) }.map { "class $it() : Instruction()" }

fun generateFloatCmpParser(): List<String> {
    val names = listOf("F32", "F64").flatMap { generateFloatCmpInsns(it) }
    val body = mutableListOf<String>()
    val opBytes = floatCmpInsnsRange.toList()
    names.forEachIndexed { index, op ->
        body += "0x${opBytes[index].toString(16)} -> $op()"
    }
    return body
}

fun generateMemOpClasses(): List<String> =
    generateMemoryOperations().map(::generateClassFromMemInsnName)

fun generateMemOpParser(): List<String> {
    val names = generateMemoryOperations()
    val body = mutableListOf<String>()
    val opBytes = memInsnRange.toList()
    names.forEachIndexed { index, op ->
        body += "0x${opBytes[index].toString(16)} -> $op(+memArg)"
    }
    return body
}