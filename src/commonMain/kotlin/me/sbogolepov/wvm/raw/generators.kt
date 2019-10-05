package me.sbogolepov.wvm.raw

val memInsnRange = 0x28..0x40

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