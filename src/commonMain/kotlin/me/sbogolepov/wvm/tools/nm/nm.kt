package me.sbogolepov.wvm.tools.nm

import me.sbogolepov.wvm.io.*
import me.sbogolepov.wvm.parser.ParserGenerator
import me.sbogolepov.wvm.parser.module

fun main(args: Array<String>) {
    val file = args[0]
    val handle = when (val result = openFile(file, FileReadMode.RB)) {
        is FileOpenResult.Error -> error("IO failed: ${result.message}")
        is FileOpenResult.Ok -> result.handle
    }
    val reader: RawDataReader = createRawDataReader(handle)
    val parser = ParserGenerator(reader)
    val module = parser.module()
}