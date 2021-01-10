package me.sbogolepov.wvm.exec

import me.sbogolepov.wvm.io.*
import me.sbogolepov.wvm.parser.ParserGenerator
import me.sbogolepov.wvm.parser.module
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

class TestInterpreter {
    @Test
    fun smoke1() {
        val path = "/Users/sergey.bogolepov/wvm/src/commonTest/resources/zzz1"
        val reader: RawDataReader = OkioRawDataReader(path.toPath())
        val parser = ParserGenerator(reader)
        val module = parser.module()

        val interpreter = Interpreter()
        interpreter.instantiate(module, "main")
        assertEquals(true, true)
    }
}