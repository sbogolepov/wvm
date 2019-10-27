package me.sbogolepov.wvm.exec

import me.sbogolepov.wvm.io.*
import me.sbogolepov.wvm.parser.ParserGenerator
import me.sbogolepov.wvm.parser.module
import kotlin.test.Test
import kotlin.test.assertEquals

class TestInterpreter {
    @Test
    fun smoke1() {
        val path = "/Users/sbogolepov/IdeaProjects/wvm/src/commonTest/resources/zzz1"
        val handle = when (val readResult = openFile(path, FileReadMode.RB)) {
            is FileOpenResult.Ok -> {
                readResult.handle
            }
            is FileOpenResult.Error -> {
                printError(readResult.message)
                return
            }
        }
        val reader: RawDataReader = createRawDataReader(handle)
        val parser = ParserGenerator(reader)
        val module = parser.module()

        val interpreter = Interpreter()
        interpreter.instantiate(module, "main")
        assertEquals(true, true)
    }
}